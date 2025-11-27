package com.example.kolla.services.impl;
import com.example.kolla.dto.MeetingCreateDTO;
import com.example.kolla.dto.search.MeetingSearchDTO;
import com.example.kolla.responses.MeetingResponse;
import com.example.kolla.responses.MeetingChartStatsResponse;
import com.example.kolla.responses.MeetingDailyStatsResponse;
import com.example.kolla.responses.MemberMeetingStatsResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        documentEditLogRepository.deleteByMeetingId(meetingId);

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

    @Override
    @Transactional(readOnly = true)
    public List<MemberMeetingStatsResponse> getMemberMeetingStats(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }

        return memberRepository.countMeetingsPerMember(startDate, endDate).stream()
                .map(MemberMeetingStatsResponse::fromProjection)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingChartStatsResponse getDailyMeetingChartStats(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEnd = endDate != null
                ? endDate
                : (startDate != null ? startDate : LocalDate.now());
        LocalDate resolvedStart = startDate != null
                ? startDate
                : resolvedEnd.minusDays(6);

        if (resolvedStart.isAfter(resolvedEnd)) {
            throw new BadRequestException("Start date must be before end date");
        }

        LocalDateTime startDateTime = resolvedStart.atStartOfDay();
        LocalDateTime endDateTime = resolvedEnd.atTime(LocalTime.MAX);

        List<MeetingDailyStatsResponse> rawStats = meetingRepository.aggregateDailyStats(startDateTime, endDateTime)
                .stream()
                .map(MeetingDailyStatsResponse::fromProjection)
                .collect(Collectors.toList());

        List<MeetingDailyStatsResponse> alignedStats = alignDailyStats(resolvedStart, resolvedEnd, rawStats);

        long totalMeetings = alignedStats.stream()
                .mapToLong(MeetingDailyStatsResponse::getMeetingCount)
                .sum();
        long totalParticipants = alignedStats.stream()
                .mapToLong(MeetingDailyStatsResponse::getParticipantCount)
                .sum();

        return MeetingChartStatsResponse.builder()
                .startDate(resolvedStart)
                .endDate(resolvedEnd)
                .totalMeetings(totalMeetings)
                .totalParticipants(totalParticipants)
                .dailyStats(alignedStats)
                .build();
    }

    private List<MeetingDailyStatsResponse> alignDailyStats(LocalDate startDate,
                                                            LocalDate endDate,
                                                            List<MeetingDailyStatsResponse> stats) {
        Map<LocalDate, MeetingDailyStatsResponse> statsByDate = stats.stream()
                .collect(Collectors.toMap(
                        MeetingDailyStatsResponse::getDate,
                        entry -> entry,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<MeetingDailyStatsResponse> aligned = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            MeetingDailyStatsResponse existing = statsByDate.get(cursor);
            if (existing == null) {
                aligned.add(MeetingDailyStatsResponse.builder()
                        .date(cursor)
                        .meetingCount(0L)
                        .participantCount(0L)
                        .build());
            } else {
                aligned.add(existing);
            }
            cursor = cursor.plusDays(1);
        }
        return aligned;
    }
}