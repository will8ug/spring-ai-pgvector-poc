package io.will.springaipoc.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("qwen")
class AiChatControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void beforeEach() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    void testChat_givenValidQuestion_whenCallingChatEndpoint_thenReturnNonEmptyResponse() {
        webTestClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"question\": \"Hello\"}")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .timeout(Duration.ofSeconds(30))
                .as(StepVerifier::create)
                .expectNextMatches(response -> response != null && !response.isEmpty())
                .thenCancel()
                .verify();
    }

    @Test
    void testStreamChat_givenValidQuestion_whenCallingStreamChatEndpoint_thenReturnStreamOfResponses() {
        Flux<String> responseFlux = webTestClient.post()
                .uri("/ai/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"question\": \"Hello\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody()
                .timeout(Duration.ofSeconds(30));

        StepVerifier.create(responseFlux)
                .expectNextCount(1) // At least one response
                .thenConsumeWhile(response -> response != null && !response.isEmpty())
                .verifyComplete();
    }

}
