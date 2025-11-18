package com.example.kolla.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchDTO {
    private String keyword; // Tìm kiếm theo fileName, userName, meetingTitle
    private Long meetingId;
    private Long userId;
    private String fileType; // FileType as string
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
