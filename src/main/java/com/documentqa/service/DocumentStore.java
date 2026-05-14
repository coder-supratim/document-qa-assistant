package com.documentqa.service;

import org.springframework.stereotype.Service;

import com.documentqa.model.DocumentResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentStore {
    private final Map<String, DocumentResponse> documents = new HashMap<>();

    /**
     * Store a document
     */
    public void save(DocumentResponse document) {
        documents.put(document.getId(), document);
    }

    /**
     * Retrieve a document by ID
     */
    public Optional<DocumentResponse> findById(String documentId) {
        return Optional.ofNullable(documents.get(documentId));
    }

    /**
     * Delete a document
     */
    public void delete(String documentId) {
        documents.remove(documentId);
    }

    /**
     * Get all documents
     */
    public Map<String, DocumentResponse> findAll() {
        return new HashMap<>(documents);
    }
}
