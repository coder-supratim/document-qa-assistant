package com.documentqa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String id;
    private String filename;
    private String content;
    private LocalDateTime uploadedAt;
    private int pageCount;
}
