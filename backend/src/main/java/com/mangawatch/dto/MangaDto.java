package com.mangawatch.dto;

import java.util.List;

public record MangaDto(
        Long id,
        String dexId,
        String title,
        String author,
        Integer releaseYear,
        String status,
        Double rating,
        String description,
        String coverUrl,
        List<String> altTitles,
        List<String> genres
){}
