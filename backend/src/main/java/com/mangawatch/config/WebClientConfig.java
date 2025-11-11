package com.mangawatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	
	@Bean
	public WebClient mangadexWebClient(@Value("${mangadex.base-url:https://api.mangadex.org}" )String baseUrl) {
		return WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.USER_AGENT, "MangaWatch/0.1")
				.codecs(configurer -> configurer
					    .defaultCodecs()
					    .maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer, increased
		        .filter((request, next) -> {
		            System.out.println(">>> Sending request to: " + request.url());
		            request.headers().forEach((k, v) -> System.out.println("Header " + k + ": " + v));
		            return next.exchange(request);
		        })
				.build();
	}
	
    @Bean
    public WebClient mangadexAuthWebClient(@Value("${mangadex.auth-url:https://auth.mangadex.org}") String authBaseUrl) {
        return WebClient.builder()
                .baseUrl(authBaseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "MangaWatch/0.1")
                .codecs(configurer -> configurer
                	    .defaultCodecs()
                	    .maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer, increased
                .filter((request, next) -> {
                    System.out.println(">>> Sending request to: " + request.url());
                    request.headers().forEach((k, v) -> System.out.println("Header " + k + ": " + v));
                    return next.exchange(request);
                })
                .build();
    }
}
