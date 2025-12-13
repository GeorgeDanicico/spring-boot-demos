package com.dg.prediction.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/chat/")
    public PredictionResponse getTextPrediction(@RequestBody PredictionRequest predictionRequest) {
        var prediction = chatService.generatePrediction(predictionRequest.text());
        return new PredictionResponse(prediction);
    }
    
    public record PredictionRequest(String text) {}

    public record PredictionResponse(String prediction) {}
}
