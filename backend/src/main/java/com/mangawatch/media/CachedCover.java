package com.mangawatch.media;

import java.time.Instant;

class CachedCover {
    byte[] data;
    String contentType;
    Instant cachedAt;

    CachedCover(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
        this.cachedAt = Instant.now();
    }
}
