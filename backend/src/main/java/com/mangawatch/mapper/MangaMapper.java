package com.mangawatch.mapper;

import com.mangawatch.dto.MangaDto;
import com.mangawatch.model.Manga;

public class MangaMapper {
    public static MangaDto toDto(Manga manga) {
        return new MangaDto(
                manga.getId(),
                manga.getDexId(),
                manga.getTitle(),
                manga.getAuthor(),
                manga.getYear(),
                manga.getStatus(),
                manga.getRating(),
                manga.getDescription(),
                manga.getCoverUrl(),
                manga.getAltTitles(),
                manga.getGenres()
        );
    }
    
    public static Manga toEntity(MangaDto dto) {
        Manga manga = new Manga();
        manga.setId(dto.id()); // or skip if DB auto-generates
        manga.setDexId(dto.dexId());
        manga.setTitle(dto.title());
        manga.setAuthor(dto.author());
        manga.setYear(dto.releaseYear());
        manga.setStatus(dto.status());
        manga.setRating(dto.rating());
        manga.setDescription(dto.description());
        manga.setCoverUrl(dto.coverUrl());
        manga.setAltTitles(dto.altTitles());
        manga.setGenres(dto.genres());
        return manga;
    }
}
