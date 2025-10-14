package io.will.springaipoc.controller;

import io.will.springaipoc.controller.model.ChatRequest;
import io.will.springaipoc.service.AiChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai")
public class AiChatController {
    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> chat(@RequestBody ChatRequest chatRequest) {
        return aiChatService.chat(chatRequest.question());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest chatRequest) {
        return aiChatService.chatStream(chatRequest.question(), chatRequest.conversationId());
    }
}
