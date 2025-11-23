package com.george.gist.model;

public record CreateGistRequest(
        String title,
        String language,
        String markdown
) { }
