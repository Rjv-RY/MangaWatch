package com.mangawatch.media;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/covers")
public class CoverController {

    private final CoverService coverService;

    public CoverController(CoverService coverService) {
        this.coverService = coverService;
    }

    @GetMapping("/{mangaId}")
    public ResponseEntity<byte[]> getCover(@PathVariable long mangaId) {
        return coverService.getCoverByMangaId(mangaId);
    }
}