package com.documentqa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {
    private List<String> topics;
    private String documentId;
    private double confidence;
}
