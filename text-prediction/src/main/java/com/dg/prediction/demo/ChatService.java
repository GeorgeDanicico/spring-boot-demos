package com.dg.prediction.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, 
        @Value("classpath:/prompts/system-message.st") Resource systemResource) {
        this.chatClient = chatClientBuilder
                            .defaultSystem(systemResource)
                            .build();
    }

    public String generatePrediction(String text) {
        return chatClient
            .prompt(text)
            .call()
            .content();
    }
    
}
