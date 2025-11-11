package com.mangawatch.mapper;

import com.mangawatch.dto.OldMangaDto;
import com.mangawatch.model.OldManga;

public class OldMangaMapper {
    public static OldMangaDto toDto(OldManga manga) {
        return new OldMangaDto(
                manga.getId(),
                manga.getTitle(),
                manga.getAuthor(),
                manga.getYear(),
                manga.getStatus(),
                manga.getRating(),
                manga.getDescription(),
                manga.getCoverUrl(),
                manga.getGenres()
        );
    }
}

