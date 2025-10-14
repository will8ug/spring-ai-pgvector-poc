package io.will.springaipoc.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@Service
public class AiChatService {
    private final ChatClient chatClient;

    private final SimpleLoggerAdvisor simpleLoggerAdvisor = new SimpleLoggerAdvisor();

    public AiChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public Mono<String> chat(String userInput) {
        Prompt prompt = constructPrompt(userInput);

        return Mono.fromCallable(() -> chatClient.prompt(prompt)
                .advisors(simpleLoggerAdvisor)
                .call()
                .content()).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<String> chatStream(String userInput, String cid) {
        final String conversationId = provisionConversationId(cid);

        Prompt prompt = constructPrompt(userInput);

        return chatClient.prompt(prompt)
                .advisors(simpleLoggerAdvisor)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    private static String provisionConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
            System.out.println("Starting new conversation: " + conversationId);
        }
        return conversationId;
    }

    private Prompt constructPrompt(String userInput) {
        Message userMessage = new UserMessage(userInput);

        return new Prompt(List.of(userMessage));
    }
}