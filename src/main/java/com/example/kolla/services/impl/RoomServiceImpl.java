package com.example.kolla.services.impl;

import com.example.kolla.dto.RoomDTO;
import com.example.kolla.exceptions.ForbiddenException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.Department;
import com.example.kolla.models.Member;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.Room;
import com.example.kolla.repositories.DepartmentRepository;
import com.example.kolla.repositories.MemberRepository;
import com.example.kolla.repositories.RoomRepository;
import com.example.kolla.dto.search.RoomSearchDTO;
import com.example.kolla.responses.RoomResponse;
import com.example.kolla.responses.ScheduleEventResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.services.RoomService;
import com.example.kolla.specifications.RoomSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final DepartmentRepository departmentRepository;
    private final Random random = new Random();

    private String generateRoomCode() {
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 9; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
            if (i % 3 == 2 && i < 8) {
                code.append("-");
            }
        }
        
        return code.toString();
    }

    @Override
    public RoomResponse createRoom(RoomDTO roomDTO) {
        Room room = modelMapper.map(roomDTO, Room.class);
        if (roomDTO.getDepartmentId()==null){
            throw new ResourceNotFoundException("Can not create room without department id");
        } else {
            Department department = this.departmentRepository.findById(
                    roomDTO.getDepartmentId()
            ).orElseThrow(() -> new ResourceNotFoundException("Can not find department with id"));
            room.setDepartment(department);
        }
        String roomCode;
        do {
            roomCode = generateRoomCode();
        } while (roomRepository.existsByRoomCode(roomCode));
        
        room.setRoomCode(roomCode);
        room = roomRepository.save(room);
        return RoomResponse.mapToResponse(room);
    }

    @Override
    public RoomResponse updateRoom(Long id, RoomDTO roomDTO) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        if (roomDTO.getRoomCode()!=null){
            room.setRoomCode(roomDTO.getRoomCode());
        }
        if (roomDTO.getRoomName()!=null){
            room.setRoomName(roomDTO.getRoomName());
        }
        if (roomDTO.getDepartmentId()!=null){
            if (!Objects.equals(room.getDepartment().getId(), roomDTO.getDepartmentId())){
                room.setDepartment(
                        this.departmentRepository.findById(
                                roomDTO.getDepartmentId()
                        ).orElseThrow(() -> new ResourceNotFoundException("Can not find department with id: "+roomDTO.getDepartmentId()))
                );
            }
        }
        room = roomRepository.save(room);
        return RoomResponse.mapToResponse(room);
    }

    @Override
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return RoomResponse.mapToResponse(room);
    }

    @Override
    public RoomResponse getByRoomCode(String roomCode) {
        return RoomResponse.mapToResponse(
                this.roomRepository.findByRoomCode(roomCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Can not find room with code: "+roomCode))
        );
    }

    @Override
    public RoomResponse getRoomByIdWithAccessCheck(Long id, Long userId) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        if (!hasUserAccessToRoom(userId, id)) {
            throw new ForbiddenException("You don't have access to this room. Please request to join first.");
        }
        
        return RoomResponse.mapToResponse(room);
    }

    @Override
    public boolean hasUserAccessToRoom(Long userId, Long roomId) {
        return memberRepository.existsByUserIdAndMeetingRoomIdAndIsActive(userId, roomId, true);
    }


    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> searchRooms(RoomSearchDTO searchDTO) {
        var spec = RoomSpecifications.withSearchCriteria(searchDTO);

        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "id";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<com.example.kolla.models.Room> roomPage = roomRepository.findAll(spec, pageable);

        PageResponse<RoomResponse> response = new PageResponse<>();
        response.setContent(roomPage.getContent().stream().map(RoomResponse::mapToResponse).collect(java.util.stream.Collectors.toList()));
        response.setPageNumber(roomPage.getNumber());
        response.setPageSize(roomPage.getSize());
        response.setTotalElements(roomPage.getTotalElements());
        response.setTotalPages(roomPage.getTotalPages());
        response.setLast(roomPage.isLast());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleEventResponse> getSchedule(Long userId) {
        return memberRepository.findMembersWithMeetingByUserId(userId).stream()
                .filter(Member::isActive)
                .map(Member::getMeeting)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        Meeting::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(ScheduleEventResponse::fromMeeting)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getRoomMembershipStatus(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        boolean joined = memberRepository.existsByUserIdAndMeetingRoomIdAndIsActive(userId, roomId, true);
        RoomResponse response = RoomResponse.mapToResponse(room);
        response.setJoined(joined);
        return response;
    }
}