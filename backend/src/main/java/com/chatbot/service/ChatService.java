package com.chatbot.service;

import com.chatbot.dto.ChatMessage;
import com.chatbot.dto.ChatResponse;
import com.chatbot.dto.GoogleAIRequest;
import com.chatbot.dto.GoogleAIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private WebClient googleAiWebClient;

    @Autowired
    private String googleAiApiKey;

    @Value("${google.ai.model:gemini-1.5-flash}")
    private String model;

    public ChatResponse generateResponse(List<ChatMessage> messages) {
        try {
            // Convert messages to Google AI format
            List<GoogleAIRequest.Content> contents = messages.stream()
                    .map(this::convertToGoogleAIContent)
                    .collect(Collectors.toList());

            // Create request
            GoogleAIRequest request = new GoogleAIRequest(
                    contents,
                    new GoogleAIRequest.GenerationConfig(0.7, 1000)
            );

            // Make API call
            Mono<GoogleAIResponse> responseMono = googleAiWebClient
                    .post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, googleAiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GoogleAIResponse.class);

            GoogleAIResponse response = responseMono.block();

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                String responseText = response.getCandidates().get(0)
                        .getContent().getParts().get(0).getText();
                return new ChatResponse(responseText, "assistant");
            } else {
                return new ChatResponse("Sorry, I didn't receive a proper response from Google AI.", "assistant");
            }

        } catch (Exception e) {
            System.err.println("Google AI API Error: " + e.getMessage());
            e.printStackTrace();
            return new ChatResponse("Sorry, I encountered an error processing your request: " + e.getMessage(), "assistant");
        }
    }

    public Flux<String> generateStreamResponse(List<ChatMessage> messages) {
        // For now, we'll simulate streaming by splitting the response
        return Flux.create(sink -> {
            try {
                ChatResponse response = generateResponse(messages);
                String[] words = response.getContent().split(" ");
                
                new Thread(() -> {
                    try {
                        for (String word : words) {
                            sink.next(word + " ");
                            Thread.sleep(50); // Simulate typing delay
                        }
                        sink.complete();
                    } catch (InterruptedException e) {
                        sink.error(e);
                    }
                }).start();
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private GoogleAIRequest.Content convertToGoogleAIContent(ChatMessage message) {
        // Convert role format
        String role = message.getRole().equals("assistant") ? "model" : "user";
        
        GoogleAIRequest.Part part = new GoogleAIRequest.Part(message.getContent());
        return new GoogleAIRequest.Content(role, List.of(part));
    }
}