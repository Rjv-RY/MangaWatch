package com.mangawatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.mangawatch.repository.MangaRepository;
import com.mangawatch.model.Manga;

@ExtendWith(MockitoExtension.class)
public class MangaServiceTest {
	
	@Mock
	private MangaRepository repo;
	
	private MangaService service;
	
	@BeforeEach
	void setUp() {
		service = new MangaService(repo);
	}
	
	//avoid boilerplate
	private Page<Manga> emptyPage(){
		return new PageImpl<>(List.of());
	}
	
	private Pageable inputPageable() {
		return PageRequest.of(0, 20);
	}
	
	@Test
	void search_withNoFilters_callsFindAll() {
		when(repo.findAll((Pageable) any())).thenReturn(emptyPage());
		
		service.search(null, null, null, "title,asc", inputPageable());
		
		verify(repo).findAll((Pageable) any());
		verify(repo, never()).searchByQuery(any(), any());
		verify(repo, never()).searchByQueryAndStatus(any(), any(), any());
		verify(repo, never()).searchWithFilters(any(), any(), any(), any());
	}
	
	@Test
	void search_withOnlyQuery_calssSearchByQuery() {
		when(repo.searchByQuery(eq("naruto"), any(Pageable.class))).thenReturn(emptyPage());
		
		service.search("naruto", null, null, "title,asc", inputPageable());
		
		verify(repo).searchByQuery(eq("naruto"), any(Pageable.class));
		verify(repo, never()).findAll(any(Pageable.class));
	}
	
	@Test
	void search_withQueryAndStatus_callsSearchByQueryAndStatus() {
        when(repo.searchByQueryAndStatus(eq("naruto"), eq("Ongoing"), any(Pageable.class)))
	        .thenReturn(emptyPage());
	
	    service.search("naruto", "Ongoing", null, "title,asc", inputPageable());
	
	    verify(repo).searchByQueryAndStatus(eq("naruto"), eq("Ongoing"), any(Pageable.class));
	    verify(repo, never()).findAll(any(Pageable.class));
	    verify(repo, never()).searchByQuery(any(), any());
	}
	
    @Test
    void search_withGenres_callsSearchWithFilters_evenIfStatusAndQueryPresent() {
    	//genres checked first, so this should work before query/status chex
    	//i.e. this test documnets that priority order
        List<String> genres = List.of("Action", "Comedy");
        when(repo.searchWithFilters(eq("naruto"), eq("Ongoing"), eq(genres), any(Pageable.class)))
            .thenReturn(emptyPage());

        service.search("naruto", "Ongoing", genres, "title,asc", inputPageable());

        verify(repo).searchWithFilters(eq("naruto"), eq("Ongoing"), eq(genres), any(Pageable.class));
        verify(repo, never()).searchByQueryAndStatus(any(), any(), any());
    }
    
    @Test
    void search_treatsBlankQueryAsNull() {
        // service does such that any truly empty query is treated as null
        when(repo.findAll(any(Pageable.class))).thenReturn(emptyPage());

        service.search("   ", null, null, "title,asc", inputPageable());

        verify(repo).findAll(any(Pageable.class));
        verify(repo, never()).searchByQuery(any(), any());
    }
    
    @Test
    void search_treatsEmptyGenresListAsNull() {
        when(repo.findAll(any(Pageable.class))).thenReturn(emptyPage());

        service.search(null, null, List.of(), "title,asc", inputPageable());

        verify(repo).findAll(any(Pageable.class));
        verify(repo, never()).searchWithFilters(any(), any(), any(), any());
    }
    
    
    // Sort field tests
    //also targets previous release year issue
    @Test
    void search_sortByYear_withNativeQuery_mapsToReleaseYearColumn() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(repo.searchByQuery(eq("naruto"), captor.capture())).thenReturn(emptyPage());

        service.search("naruto", null, null, "year,asc", inputPageable());

        Sort.Order order = captor.getValue().getSort().getOrderFor("release_year");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }
    

    @Test
    void search_sortByYear_withNoFilters_mapsToReleaseYearEntityField() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(repo.findAll(captor.capture())).thenReturn(emptyPage());

        service.search(null, null, null, "year,desc", inputPageable());

        Sort.Order order = captor.getValue().getSort().getOrderFor("releaseYear");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void search_unknownSortField_fallsBackToTitle() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(repo.findAll(captor.capture())).thenReturn(emptyPage());

        service.search(null, null, null, "totallyMadeUpField,asc", inputPageable());

        assertThat(captor.getValue().getSort().getOrderFor("title")).isNotNull();
    }
}
