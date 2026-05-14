package com.documentqa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerResponse {
    private String question;
    private String answer;
    private String documentId;
    private double confidence;
}
