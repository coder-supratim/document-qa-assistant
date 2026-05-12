package com.example.controller;

import com.example.model.DocumentResponse;
import com.example.service.DocumentParsingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentParsingService documentParsingService;

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

            // Create response
            DocumentResponse response = new DocumentResponse(
                    documentParsingService.generateDocumentId(),
                    file.getOriginalFilename(),
                    content,
                    LocalDateTime.now(),
                    pageCount
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
