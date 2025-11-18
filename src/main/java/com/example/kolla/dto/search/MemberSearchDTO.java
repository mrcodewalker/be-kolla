package com.example.kolla.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchDTO {
    private String keyword; // Tìm kiếm theo tên user, email, tên phòng
    private Long userId;
    private Long roomId;
    private Long meetingId;
    private Long roleId;
    private Boolean isActive;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
