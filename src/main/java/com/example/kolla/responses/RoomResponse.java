package com.example.kolla.responses;

import com.example.kolla.models.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomName;
    private String roomCode;
    private DepartmentResponse departmentResponse;
    private boolean isJoined;
    public static RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .roomCode(room.getRoomCode())
                .departmentResponse(room.getDepartment() != null
                        ? DepartmentResponse.mapToResponse(room.getDepartment())
                        : null
                )
                .build();
    }
}