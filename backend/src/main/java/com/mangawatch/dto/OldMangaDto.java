package com.mangawatch.dto;

import java.util.List;

public record OldMangaDto(
        Long id,
        String title,
        String author,
        Integer year,
        String status,
        Double rating,
        String description,
        String coverUrl,
        List<String> genres
){}

