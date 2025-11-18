package com.example.kolla.services;

import com.example.kolla.dto.RoomDTO;
import com.example.kolla.dto.search.RoomSearchDTO;
import com.example.kolla.responses.RoomResponse;
import com.example.kolla.responses.ScheduleEventResponse;
import com.example.kolla.responses.PageResponse;
import java.util.List;

public interface RoomService {
    RoomResponse createRoom(RoomDTO roomDTO);
    RoomResponse updateRoom(Long id, RoomDTO roomDTO);
    void deleteRoom(Long id);
    RoomResponse getRoomById(Long id);
    RoomResponse getByRoomCode(String roomCode);
    List<RoomResponse> getAllRooms();
    PageResponse<RoomResponse> searchRooms(RoomSearchDTO searchDTO);
    RoomResponse getRoomMembershipStatus(Long roomId, Long userId); // Check status for a given user in context of room via meeting memberships
    List<ScheduleEventResponse> getSchedule(Long userId);
    RoomResponse getRoomByIdWithAccessCheck(Long id, Long userId);
    boolean hasUserAccessToRoom(Long userId, Long roomId);
}