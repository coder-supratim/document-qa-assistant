package com.documentqa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.documentqa.model.AskQuestionRequest;
import com.documentqa.model.DocumentResponse;
import com.documentqa.model.QuestionAnswerResponse;
import com.documentqa.service.DocumentAIService;
import com.documentqa.service.DocumentParsingService;
import com.documentqa.service.DocumentStore;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentParsingService documentParsingService;
    private final DocumentStore documentStore;
    private final DocumentAIService documentAIService;

    /**
     * Upload a document (PDF or text file)
     * 
     * @param file the document file to upload
     * @return DocumentResponse with document metadata
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validate file type
            String contentType = file.getContentType();
            if (!isValidFileType(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Extract content from file
            String content = documentParsingService.extractContent(file);
            
            // Get page count
            int pageCount = documentParsingService.getPageCount(file);

                // Upload source file to OpenAI and keep the returned file id
                String openAiFileId = documentAIService.uploadDocument(file);

            // Create response
            DocumentResponse response = new DocumentResponse(
                    documentParsingService.generateDocumentId(),
                    file.getOriginalFilename(),
                    content,
                    openAiFileId,
                    LocalDateTime.now(),
                    pageCount
            );

            // Save to document store
            documentStore.save(response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ask a question about a document
     * 
     * @param documentId the ID of the document
     * @param request the question request
     * @return QuestionAnswerResponse with the answer
     */
    @PostMapping("/{documentId}/ask")
    public ResponseEntity<QuestionAnswerResponse> askQuestion(
            @PathVariable String documentId,
            @RequestBody AskQuestionRequest request) {
        
        // Retrieve document from store
        var document = documentStore.findById(documentId);
        
        if (document.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        DocumentResponse doc = document.get();
        
        // Ask question using AI service
        QuestionAnswerResponse answer = documentAIService.askQuestion(
                doc.getContent(),
                request.getQuestion(),
                documentId
        );

        return ResponseEntity.ok(answer);
    }

    /**
     * Get document summary
     * 
     * @param documentId the ID of the document
     * @return QuestionAnswerResponse with the summary
     */
    @GetMapping("/{documentId}/summary")
    public ResponseEntity<QuestionAnswerResponse> getDocumentSummary(@PathVariable String documentId) {
        // Retrieve document from store
        var document = documentStore.findById(documentId);
        
        if (document.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        DocumentResponse doc = document.get();
        
        // Ask for summary
        QuestionAnswerResponse summary = documentAIService.askQuestion(
                doc.getContent(),
                "Provide a concise summary of this document.",
                documentId
        );

        return ResponseEntity.ok(summary);
    }

    /**
     * Extract topics from document
     * 
     * @param documentId the ID of the document
     * @return QuestionAnswerResponse with extracted topics
     */
    @GetMapping("/{documentId}/topics")
    public ResponseEntity<QuestionAnswerResponse> extractTopics(@PathVariable String documentId) {
        // Retrieve document from store
        var document = documentStore.findById(documentId);
        
        if (document.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        DocumentResponse doc = document.get();
        
        // Ask for topics
        QuestionAnswerResponse topics = documentAIService.askQuestion(
                doc.getContent(),
                "List the main topics and key points covered in this document.",
                documentId
        );

        return ResponseEntity.ok(topics);
    }

    /**
     * List all documents
     * 
     * @return Map of all documents
     */
    @GetMapping
    public ResponseEntity<Map<String, DocumentResponse>> listDocuments() {
        Map<String, DocumentResponse> documents = documentStore.findAll();
        return ResponseEntity.ok(documents);
    }

    /**
     * Delete a document
     * 
     * @param documentId the ID of the document to delete
     * @return Response status
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        var document = documentStore.findById(documentId);
        
        if (document.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        documentStore.delete(documentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validate if the uploaded file type is supported
     */
    private boolean isValidFileType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("application/pdf") || 
               contentType.equals("text/plain") ||
               contentType.startsWith("text/");
    }
}
