package com.example.kolla.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchDTO {
    private String keyword; // Search by roomName, roomCode, department name
    private Long departmentId;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}













