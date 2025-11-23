package com.george.gist.store;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.george.gist.model.Gist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
public class S3GistStore {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final S3Client s3Client;

    @Value("spring.aws.bucket")
    private String bucketName;

    public S3GistStore(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void save(Gist gist) {
        try {
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }
            var key = "gists/" + gist.id() + ".json";
            var data = mapper.writeValueAsBytes(gist);
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build(), RequestBody.fromBytes(data));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save gist", e);
        }
    }

    public Gist find(String id) {
        var key = "gists/" + id + ".json";
        try (var obj = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build())) {
            return mapper.readValue(obj, Gist.class);
        } catch (NoSuchKeyException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read gist", e);
        }
    }

    public List<String> listIds() {
        var res = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("gists/")
                .build());
        return res.contents().stream()
                .map(S3Object::key)
                .map(k -> k.substring(k.lastIndexOf('/') + 1, k.indexOf(".json")))
                .toList();
    }

    private boolean bucketExists(String name) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(name).build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    private void createBucket(String name) {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(name).build());
    }
}