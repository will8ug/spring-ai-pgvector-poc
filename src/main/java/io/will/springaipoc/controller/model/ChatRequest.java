package io.will.springaipoc.controller.model;

public record ChatRequest(
        String question,
        String conversationId
) {
}
