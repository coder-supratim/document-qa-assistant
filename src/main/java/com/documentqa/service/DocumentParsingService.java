package com.documentqa.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentParsingService {

    /**
     * Extracts text content from uploaded file (text or binary files)
     */
    public String extractContent(MultipartFile file) throws IOException {
        // For now, store the file as a string representation
        // Note: PDF content extraction would require proper parsing
        byte[] fileBytes = file.getBytes();
        
        String contentType = file.getContentType();
        if (contentType != null && contentType.equals("application/pdf")) {
            // For PDFs, return a placeholder indicating PDF file was uploaded
            
            return String.format("[PDF Document: %s - %d bytes]", file.getOriginalFilename(), fileBytes.length);
        } else {
            // For text files, return the content as string
            return new String(fileBytes);
        }
    }

    /**
     * Counts the number of pages (for PDFs)
     */
    public int getPageCount(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        
        // For text files and other file types, return 1
        return 1;
    }

    /**
     * Generates a unique document ID
     */
    public String generateDocumentId() {
        return UUID.randomUUID().toString();
    }
}
