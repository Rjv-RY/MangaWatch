//package com.mangawatch.apicaller;
//
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import com.mangawatch.dto.AuthResponse;
//
//import reactor.core.publisher.Mono;
//
//@Service
//public class MangadexCaller {
//    private final WebClient webClient;
//
//    private final String clientId;
//
//    private final String clientSecret;
//
//    private final String username;
//
//    private final String password;
//    
//    private String refreshToken;
//    
//    private volatile String accessToken;
//    
//    public String getAccessToken() {
//        return this.accessToken;
//    }
//    
//    public void setAccessToken(String token) {
//        this.accessToken = token;
//    }
//    
//    public MangadexCaller(@Qualifier("mangadexWebClient") WebClient webClient,
//            @Value("${mangadex.client-id}") String clientId,
//            @Value("${mangadex.client-secret}") String clientSecret,
//            @Value("${mangadex.username}") String username,
//            @Value("${mangadex.password}") String password) {
//        this.webClient = webClient;
//        this.clientId = clientId;
//        this.clientSecret = clientSecret;
//        this.username = username;
//        this.password = password;
//    }
//    
//    // test if works (it does)
//    public Mono<String> pingApi() {
//        return webClient
//                .get()
//                .uri("/ping")
//                .retrieve()
//                .bodyToMono(String.class);
//    }
//    
//    public Mono<String> fetchMangaSample(){
//    	return webClient
//    			.get()
//    			.uri(uriBuilder -> uriBuilder
//    					.path("/manga")
//    					.queryParam("limit", 5)
//    					.build())
//    			.headers(headers -> headers.setBearerAuth(this.accessToken))
//    			.retrieve()
//    			.onStatus(HttpStatusCode::isError,
//                        resp -> resp.bodyToMono(String.class)
//                                    .flatMap(body -> Mono.error(new RuntimeException("Error: " + body))))
//                .bodyToMono(String.class);
//    }
//    
//    public Mono<AuthResponse> login() {
//        return webClient
//                .post()
//                .uri("https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token")
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .body(BodyInserters.fromFormData("grant_type", "password")
//                        .with("username", username)
//                        .with("password", password)
//                        .with("client_id", clientId)
//                        .with("client_secret", clientSecret))
//                .retrieve()
//                .bodyToMono(AuthResponse.class)
//                .doOnNext(authResponse -> {
//                    // set in-memory tokens in MangadexCaller
//                    this.accessToken = authResponse.getAccessToken();
//                    this.refreshToken = authResponse.getRefreshToken();
//
//                    System.out.println("Access token set: " + this.accessToken);
//                    System.out.println("Refresh token set: " + this.refreshToken);
//                });
//    }
//    
//    public Mono<AuthResponse> refresh() {
//        return webClient
//                .post()
//                .uri("https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token")
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
//                        .with("refresh_token", this.refreshToken)
//                        .with("client_id", clientId)
//                        .with("client_secret", clientSecret))
//                .retrieve()
//                .bodyToMono(AuthResponse.class)
//                .doOnNext(authResponse -> {
//                    this.accessToken = authResponse.getAccessToken();
//                    this.refreshToken = authResponse.getRefreshToken();
//                });
//    }
//}
