package com.george.gist.controller;

import java.time.Instant;
import java.util.Locale;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.george.gist.model.CreateGistRequest;
import com.george.gist.model.Gist;
import com.george.gist.service.MarkdownService;
import com.george.gist.store.S3GistStore;

import de.huxhorn.sulky.ulid.ULID;

@RestController("/")
public class GistController {
    private static final ULID ULID = new ULID();

    private final S3GistStore s3GistStore;
    private final MarkdownService markdownService;
    private final SpringTemplateEngine templateEngine;

    public GistController(S3GistStore s3GistStore, MarkdownService markdownService, SpringTemplateEngine templateEngine) {
        this.s3GistStore = s3GistStore;
        this.markdownService = markdownService;
        this.templateEngine = templateEngine;
    }

    @PostMapping(value = "/gists", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody CreateGistRequest request) {
        if (request == null || request.markdown() == null || request.markdown().isBlank()) {
            throw new RuntimeException("Markdown required");
        }

        var gist = new Gist(
            ULID.nextULID().toLowerCase(), 
            request.title(), 
            request.language(), 
            request.markdown(), 
            markdownService.toSafeHtml(request.markdown()),
            Instant.now());

        s3GistStore.save(gist);
        return ResponseEntity.ok(gist);
    }
    
    @GetMapping("/gists/{id}.json")
    public Gist get(@PathVariable("id") String id) {
        var gist = s3GistStore.find(id);
        if (gist == null)
            throw new RuntimeException();
        return gist;
    }

    @GetMapping(value = "/g/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String page(@PathVariable("id") String id) {
        var gist = s3GistStore.find(id);
        if (gist == null)
            throw new RuntimeException();

        var context = new Context(Locale.getDefault());
        context.setVariable("gist", gist);
        return templateEngine.process("gist-template", context);
    }
}
