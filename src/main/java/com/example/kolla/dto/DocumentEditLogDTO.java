package com.example.kolla.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEditLogDTO {
    private Long id;
    private Long documentId;
    private Long userId;
    private String action;
    private String changes;
    private LocalDateTime editedAt;
    private UserDTO user;
    private DocumentDTO document;
}