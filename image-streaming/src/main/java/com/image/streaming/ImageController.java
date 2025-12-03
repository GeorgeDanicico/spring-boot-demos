package com.image.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController()
public class ImageController {

    private static final String IMAGE_ROOT = "src/main/resources/images";

    @GetMapping("/api/image/stream/{filename}")
    public void streamImage(HttpServletResponse response,
                            @PathVariable("filename") String filename) throws IOException, InterruptedException {

        File imageFile = new File(IMAGE_ROOT + "/" + filename);
        if (!imageFile.exists() || !imageFile.isFile()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/jpeg");
        response.setHeader("Content-Length", String.valueOf(imageFile.length()));

        try (InputStream in = new FileInputStream(imageFile);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
                Thread.sleep(50); // We simulate a progressive image generation

            }
        } catch (IOException ex) {
            throw ex;
        }
    }
}
