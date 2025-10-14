package io.will.springaipoc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    @Mock
    private AssistantMessage assistantMessage;

    private AiChatService aiChatService;

    @BeforeEach
    void setUp() {
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        aiChatService = new AiChatService(chatClient);
    }

    @Test
    void testChat_givenValidInput_whenCallingChat_thenReturnMockedResponse() {
        String userInput = "What is the capital of France?";
        String expectedResponse = "The capital of France is Paris.";
        
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(expectedResponse);

        Mono<String> result = aiChatService.chat(userInput);

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();

        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testStreamChat_givenValidInput_whenCallingStreamChat_thenReturnMockedStreamResponse() {
        String userInput = "Tell me about the geography of Japan.";
        String[] expectedResponses = {"Japan is an island nation", "located in East Asia", "consisting of four main islands."};
        
        Flux<ChatResponse> mockStream = Flux.fromArray(expectedResponses)
                .map(response -> {
                    AssistantMessage msg = new AssistantMessage(response);
                    Generation gen = new Generation(msg);
                    return new ChatResponse(List.of(gen));
                });
        
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockStream);

        Flux<String> result = aiChatService.chatStream(userInput, "test-conversation-id");

        StepVerifier.create(result)
                .expectNext(expectedResponses)
                .verifyComplete();

        verify(chatModel).stream(any(Prompt.class));
    }
}