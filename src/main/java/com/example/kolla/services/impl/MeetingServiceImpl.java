package com.example.kolla.services.impl;
import com.example.kolla.dto.MeetingCreateDTO;
import com.example.kolla.dto.search.MeetingSearchDTO;
import com.example.kolla.responses.MeetingResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.*;
import com.example.kolla.repositories.*;
import com.example.kolla.services.MeetingService;
import com.example.kolla.specifications.MeetingSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final NotificationRepository notificationRepository;
    private final MeetingMessageRepository meetingMessageRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final DocumentRepository documentRepository;
    private final DocumentEditLogRepository documentEditLogRepository;
    private final RecordingRepository recordingRepository;

    @Override
    @Transactional
    public MeetingResponse createMeeting(Long userId, MeetingCreateDTO createDTO) {
        validateMeetingTime(createDTO.getStartTime(), createDTO.getEndTime());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Room room = roomRepository.findById(createDTO.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + createDTO.getRoomId()));

        Meeting meeting = new Meeting();
        meeting.setTitle(createDTO.getTitle());
        meeting.setDescription(createDTO.getDescription());
        meeting.setStartTime(createDTO.getStartTime());
        meeting.setEndTime(createDTO.getEndTime());
        meeting.setCreatedBy(user);
        meeting.setRoom(room);
        String meetingCode = generateMeetingCode();
        meeting.setMeetingCode(meetingCode);
        meeting.setMeetingLink(generateMeetingLink(meetingCode));

        Meeting savedMeeting = meetingRepository.save(meeting);

        // Auto add creator as active member with role (fallback to USER)
        Member creatorMembership = new Member();
        creatorMembership.setUser(user);
        creatorMembership.setMeeting(savedMeeting);
        creatorMembership.setActive(true);
        Role memberRole = user.getRole() != null ? user.getRole() : roleRepository.findByName("USER");
        creatorMembership.setRole(memberRole);
        memberRepository.save(creatorMembership);

        return MeetingResponse.mapToResponse(savedMeeting);
    }

    @Override
    @Transactional
    public MeetingResponse updateMeeting(Long id, MeetingCreateDTO updateDTO) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        validateMeetingTime(updateDTO.getStartTime(), updateDTO.getEndTime());

        if (updateDTO.getTitle() != null) {
            meeting.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getRoomId()!=null&&!updateDTO.getRoomId().equals(meeting.getRoom().getId())){
            meeting.setRoom(
                    this.roomRepository.findById(updateDTO.getRoomId())
                            .orElseThrow(() -> new ResourceNotFoundException("Can not find room with ID: "+updateDTO.getRoomId()))
            );
        }
        if (updateDTO.getDescription()!=null) {
            meeting.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStartTime()!=null){
            meeting.setStartTime(updateDTO.getStartTime());
        }
        if (updateDTO.getEndTime()!=null) {
            meeting.setEndTime(updateDTO.getEndTime());
        }
        this.validateMeetingTime(updateDTO.getStartTime(), updateDTO.getEndTime());

        Meeting updatedMeeting = meetingRepository.save(meeting);
        return MeetingResponse.mapToResponse(updatedMeeting);
    }

    @Override
    public MeetingResponse getMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
        return MeetingResponse.mapToResponse(meeting);
    }

    @Override
    public List<MeetingResponse> getMeetingByRoomId(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId);
        }

        return meetingRepository.findByRoomIdOrderByStartTimeDesc(roomId).stream()
            .map(MeetingResponse::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<MeetingResponse> getMeetingByRoomIdWithStatus(Long roomId, Long userId, String status) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId);
        }

        List<Meeting> meetings = meetingRepository.findByRoomIdOrderByStartTimeDesc(roomId);

        List<Member> memberships = memberRepository.findMembersWithMeetingByUserId(userId);
        java.util.Map<Long, Member> membershipByMeetingId = memberships.stream()
                .collect(Collectors.toMap(m -> m.getMeeting().getId(), m -> m, (a, b) -> a));

        List<MeetingResponse> meetingResponses = meetings.stream()
                .map(m -> {
                    Member mem = membershipByMeetingId.get(m.getId());
                    String membershipStatus = (mem == null) ? "NONE" : (mem.isActive() ? "APPROVED" : "PENDING");
                    return MeetingResponse.mapToResponse(m, membershipStatus);
                })
                .collect(Collectors.toList());

        if (status != null && !status.isBlank()) {
            meetingResponses = meetingResponses.stream()
                    .filter(r -> status.equalsIgnoreCase(r.getMembershipStatus()))
                    .collect(Collectors.toList());
        }

        meetingResponses.sort((r1, r2) -> {
            String status1 = r1.getMembershipStatus();
            String status2 = r2.getMembershipStatus();
            return Integer.compare(getStatusScore(status1), getStatusScore(status2));
        });

        return meetingResponses;
    }

    @Override
    public Meeting findById(Long id) {
        return meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MeetingResponse> searchMeetings(MeetingSearchDTO searchDTO) {
        // Tạo specification từ search criteria
        Specification<Meeting> spec = MeetingSpecifications.withSearchCriteria(searchDTO);
        
        // Tạo sort
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "createdAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        // Tạo pageable
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Thực hiện query
        Page<Meeting> meetingPage = meetingRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang response
        PageResponse<MeetingResponse> response = new PageResponse<>();
        response.setContent(meetingPage.getContent().stream()
            .map(MeetingResponse::mapToResponse)
            .collect(Collectors.toList()));
        response.setPageNumber(meetingPage.getNumber());
        response.setPageSize(meetingPage.getSize());
        response.setTotalElements(meetingPage.getTotalElements());
        response.setTotalPages(meetingPage.getTotalPages());
        response.setLast(meetingPage.isLast());
        
        return response;
    }

    @Override
    public List<MeetingResponse> getMeetingsPreviewByUser(Long userId) {
        // Fetch user memberships (meeting joined or requested)
        List<Member> memberships = memberRepository.findMembersWithMeetingByUserId(userId);
        java.util.Map<Long, Member> membershipByMeetingId = memberships.stream()
                .collect(Collectors.toMap(m -> m.getMeeting().getId(), m -> m, (a, b) -> a));

        // Load all meetings for preview; status NONE for those without a membership
        List<Meeting> allMeetings = meetingRepository.findAll();

        // Sort desc: prefer membership.createdAt when present, else meeting.startTime
        return allMeetings.stream()
                .sorted((m1, m2) -> {
                    Member mm1 = membershipByMeetingId.get(m1.getId());
                    Member mm2 = membershipByMeetingId.get(m2.getId());
                    java.time.LocalDateTime t1 = mm1 != null ? mm1.getCreatedAt() : m1.getStartTime();
                    java.time.LocalDateTime t2 = mm2 != null ? mm2.getCreatedAt() : m2.getStartTime();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t2.compareTo(t1);
                })
                .map(meeting -> {
                    MeetingResponse resp = MeetingResponse.mapToResponse(meeting);
                    Member mem = membershipByMeetingId.get(meeting.getId());
                    if (mem == null) {
                        resp.setMembershipStatus("NONE");
                    } else {
                        resp.setMembershipStatus(mem.isActive() ? "APPROVED" : "PENDING");
                    }
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMeeting(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        Long meetingId = meeting.getId();

        meetingMessageRepository.deleteByMeetingId(meetingId);
        attendanceLogRepository.deleteByMeetingId(meetingId);
        notificationRepository.deleteByMeetingId(meetingId);
        recordingRepository.deleteByMeetingId(meetingId);

        List<Document> documents = documentRepository.findByMeetingId(meetingId);
        if (!documents.isEmpty()) {
            List<Long> documentIds = documents.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());
            documentIds.forEach(documentEditLogRepository::deleteByDocumentId);
            documentRepository.deleteAll(documents);
        }

        meetingRepository.delete(meeting);
    }

    @Override
    public void validateMeetingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Meeting start and end time cannot be null");
        }

        if (startTime.isAfter(endTime)) {
            throw new BadRequestException("Meeting start time must be before end time");
        }
    }

    private String generateMeetingCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code;
        do {
            code = new StringBuilder();
            // Generate first 4 characters
            for (int i = 0; i < 4; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            code.append("-");
            // Generate middle 4 characters
            for (int i = 0; i < 4; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            code.append("-");
            // Generate last 4 characters
            for (int i = 0; i < 4; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
        } while (meetingRepository.existsByMeetingCode(code.toString()));
        return code.toString();
    }

    private String generateMeetingLink(String meetingCode) {
        return "KOLLA-MEET-" + meetingCode.toUpperCase();
    }

    private int getStatusScore(String status) {
        if (status == null) {
            return 3;
        }
        switch (status.toUpperCase()) {
            case "APPROVED":
                return 1;
            case "PENDING":
                return 2;
            case "NONE":
                return 3;
            default:
                return 4;
        }
    }

    @Override
    @Transactional
    public void updateIsMeetingToTrue(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));
        meeting.setMeeting(true);
        meetingRepository.save(meeting);
    }
}