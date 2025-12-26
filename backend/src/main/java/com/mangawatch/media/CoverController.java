package com.mangawatch.media;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/covers")
public class CoverController {

    private final CoverService coverService;

    public CoverController(CoverService coverService) {
        this.coverService = coverService;
    }

    @GetMapping(value = "/{mangaId}", produces = {
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    })
    public ResponseEntity<Flux<DataBuffer>> getCover(@PathVariable long mangaId) {
        return coverService.getCoverByMangaId(mangaId);
    }
}