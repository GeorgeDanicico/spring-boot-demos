package com.george.gist.model;

import java.time.Instant;

public record Gist(
        String id,
        String title,
        String language,
        String markdown,
        String html,
        Instant createdAt
) {
}
