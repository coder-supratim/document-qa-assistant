package com.documentqa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.documentqa.model.QuestionAnswerResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentAIService {

    private final RestTemplate restTemplate;
    
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;
    
    @Value("${app.openai.model}")
    private String model;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_FILES_API_URL = "https://api.openai.com/v1/files";

    public String uploadDocument(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("purpose", "assistants");
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_FILES_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload document to OpenAI: " + e.getMessage(), e);
        }
    }

    /**
     * Ask a question about document content using OpenAI API
     * 
     * @param documentContent The content of the document
     * @param question The question to ask
     * @param documentId The ID of the document
     * @return QuestionAnswerResponse containing the answer and confidence
     */
    public QuestionAnswerResponse askQuestion(String documentContent, String question, String documentId) {
        try {
            String systemPrompt = """
                    You are a helpful document analysis assistant. Based on the provided document content, 
                    answer the following question accurately and concisely.""";

            String userMessage = String.format("""
                    Document Content:
                    %s
                    
                    Question: %s
                    
                    Please provide a clear and accurate answer based on the document content.""", 
                    documentContent, question);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
            ));

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openAiApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call OpenAI API
            String response = restTemplate.postForObject(OPENAI_API_URL, request, String.class);

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String answer = root.path("choices").get(0).path("message").path("content").asText();

            // Calculate confidence
            double confidence = calculateConfidence(documentContent, question, answer);

            return new QuestionAnswerResponse(
                    question,
                    answer,
                    documentId,
                    confidence
            );
        } catch (Exception e) {
            // Return error response with low confidence
            return new QuestionAnswerResponse(
                    question,
                    "Error processing question: " + e.getMessage(),
                    documentId,
                    0.0
            );
        }
    }

    /**
     * Calculate confidence score (simple heuristic)
     * In production, this could use more sophisticated methods
     */
    private double calculateConfidence(String documentContent, String question, String answer) {
        if (answer == null || answer.isEmpty() || answer.contains("Error")) {
            return 0.0;
        }

        // Simple heuristic: if document contains words from question, higher confidence
        String lowerContent = documentContent.toLowerCase();
        String lowerQuestion = question.toLowerCase();
        String[] questionWords = lowerQuestion.split("\\s+");
        
        int matchCount = 0;
        for (String word : questionWords) {
            if (word.length() > 3 && lowerContent.contains(word)) {
                matchCount++;
            }
        }

        double confidence = Math.min(0.95, 0.5 + (matchCount * 0.1));
        return Math.round(confidence * 100.0) / 100.0;
    }
}


