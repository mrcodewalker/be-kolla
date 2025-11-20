package com.example.kolla.dto;

import com.example.kolla.models.DocumentEditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEditLogDTO {
    private Long id;
    private Long meetingId;
    private String meetingTitle;
    private Long editedById;
    private String editedByName;
    private LocalDateTime editedAt;
    private String changeSummary;

    public static DocumentEditLogDTO fromEntity(DocumentEditLog log) {
        if (log == null) {
            return null;
        }
        return DocumentEditLogDTO.builder()
                .id(log.getId())
                .meetingId(log.getMeeting() != null ? log.getMeeting().getId() : null)
                .meetingTitle(log.getMeeting() != null ? log.getMeeting().getTitle() : null)
                .editedById(log.getEditedBy() != null ? log.getEditedBy().getId() : null)
                .editedByName(log.getEditedBy() != null ? log.getEditedBy().getName() : null)
                .editedAt(log.getEditedAt())
                .changeSummary(log.getChangeSummary())
                .build();
    }
}