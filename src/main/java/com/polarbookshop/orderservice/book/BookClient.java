package com.polarbookshop.orderservice.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {

    private static final Logger log = LoggerFactory.getLogger(BookClient.class);

    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        log.info("Getting book by ISBN {}", isbn);
        return webClient
            .get()
            .uri(BOOKS_ROOT_API + isbn)
            .retrieve()
            .bodyToMono(Book.class)
            .timeout(Duration.ofSeconds(3), Mono.empty())
            .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
            .onErrorResume(Exception.class, exception -> Mono.empty());
    }
}
