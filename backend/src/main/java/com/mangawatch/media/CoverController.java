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

    @GetMapping("/{dexId}/{fileName}")
    public ResponseEntity<byte[]> getCover(
            @PathVariable String dexId,
            @PathVariable String fileName
    ) {
        return coverService.getCover(dexId, fileName);
    }
}