package com.example.kolla.services.impl;

import com.example.kolla.dto.search.AttendanceLogSearchDTO;
import com.example.kolla.responses.AttendanceLogResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.specifications.AttendanceLogSpecifications;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.AttendanceLog;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.User;
import com.example.kolla.repositories.AttendanceLogRepository;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.services.AttendanceLogService;
import com.example.kolla.services.UserSessionService;
import com.example.kolla.services.impl.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.example.kolla.utils.DateTimeUtils;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceLogServiceImpl implements AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserSessionService userSessionService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AttendanceLogResponse createAttendanceLog(Long meetingId, Long userId, HttpServletRequest request) {
        AttendanceLog log = new AttendanceLog();
        log.setJoinAt(DateTimeUtils.now());
        log.setDeviceInfo(request.getHeader("User-Agent"));
        log.setIpAddress(userSessionService.getClientIp(request));
        log.setLocation(userSessionService.getLocationFromIp(userSessionService.getClientIp(request)));

        AttendanceLog savedLog = attendanceLogRepository.save(log);
        return AttendanceLogResponse.mapToResponse(savedLog);
    }

    @Override
    @Transactional
    public AttendanceLogResponse updateLeaveTime(Long logId, LocalDateTime leaveTime) {
        AttendanceLog log = attendanceLogRepository.findById(logId)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance log not found with id: " + logId));

        if (leaveTime.isBefore(log.getJoinAt())) {
            throw new BadRequestException("Leave time cannot be before join time");
        }

        log.setLeaveAt(leaveTime);
        AttendanceLog updatedLog = attendanceLogRepository.save(log);
        return AttendanceLogResponse.mapToResponse(updatedLog);
    }

    @Override
    public AttendanceLogResponse getAttendanceLogById(Long id) {
        AttendanceLog log = attendanceLogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance log not found with id: " + id));
        return AttendanceLogResponse.mapToResponse(log);
    }

    @Override
    public List<AttendanceLogResponse> getAttendanceLogsByMeeting(Long meetingId) {
        return attendanceLogRepository.findByMeetingId(meetingId).stream()
            .map(AttendanceLogResponse::mapToResponse)
            .toList();
    }

    @Override
    public List<AttendanceLogResponse> getAttendanceLogsByUser(Long userId) {
        return attendanceLogRepository.findByUserId(userId).stream()
            .map(AttendanceLogResponse::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceLogResponse> searchAttendanceLogs(AttendanceLogSearchDTO searchDTO) {
        // Tạo specification từ search criteria
        Specification<AttendanceLog> spec = AttendanceLogSpecifications.withSearchCriteria(searchDTO);
        
        // Tạo sort
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "joinAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        // Tạo pageable
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        PageRequest pageable = PageRequest.of(page, size, sort);
        
        // Thực hiện query
        Page<AttendanceLog> logPage = attendanceLogRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang response
        List<AttendanceLogResponse> logResponses = logPage.getContent().stream()
                .map(AttendanceLogResponse::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        // Tạo PageResponse
        PageResponse<AttendanceLogResponse> response = new PageResponse<>();
        response.setContent(logResponses);
        response.setPageNumber(logPage.getNumber());
        response.setPageSize(logPage.getSize());
        response.setTotalElements(logPage.getTotalElements());
        response.setTotalPages(logPage.getTotalPages());
        response.setLast(logPage.isLast());
        
        return response;
    }

    @Override
    @Transactional
    public void deleteAttendanceLog(Long id) {
        if (!attendanceLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance log not found with id: " + id);
        }
        attendanceLogRepository.deleteById(id);
    }

    @Override
    public double calculateAttendanceRate(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

        List<AttendanceLog> logs = attendanceLogRepository.findByUserId(userId);
        
        if (logs.isEmpty()) {
            return 0.0;
        }

        Duration meetingDuration = Duration.between(meeting.getStartTime(), meeting.getEndTime());
        Duration totalAttendance = Duration.ZERO;

        for (AttendanceLog log : logs) {
            if (log.getLeaveAt() != null) {
                totalAttendance = totalAttendance.plus(
                    Duration.between(log.getJoinAt(), log.getLeaveAt())
                );
            }
        }

        return (double) totalAttendance.toMinutes() / meetingDuration.toMinutes() * 100;
    }

    @Override
    @Transactional
    public AttendanceLogResponse updateLeaveTimeWithToken(Long logId, String token) {
        try {
            // Kiểm tra và lấy thông tin user từ token
            String userEmail = jwtService.extractUsername(token);
            if (userEmail == null) {
                throw new BadRequestException("Invalid token");
            }

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            // Tìm attendance log
            AttendanceLog log = attendanceLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance log not found with id: " + logId));

            // Kiểm tra xem user có quyền update log này không
            if (log.getUser() != null && !log.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("You don't have permission to update this attendance log");
            }

            // Sử dụng thời gian hiện tại
            LocalDateTime now = DateTimeUtils.now();
            if (now.isBefore(log.getJoinAt())) {
                throw new BadRequestException("Current time cannot be before join time");
            }

            log.setLeaveAt(now);
            AttendanceLog updatedLog = attendanceLogRepository.save(log);
            return AttendanceLogResponse.mapToResponse(updatedLog);
        } catch (Exception e) {
            throw new BadRequestException("Failed to update leave time: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AttendanceLogResponse createAttendanceLogWithToken(String token, String meetLink, String action, HttpServletRequest request) {
        try {
            // Tìm meeting từ meetLink
            Meeting meeting = meetingRepository.findByMeetingLink(meetLink)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with link: " + meetLink));

            // Nếu có token, lấy thông tin user
            User user = null;
            if (token != null && !token.isEmpty()) {
                String userEmail = jwtService.extractUsername(token);
                if (userEmail != null) {
                    user = userRepository.findByEmail(userEmail)
                            .orElse(null);
                }
            }

            // Tạo attendance log
            AttendanceLog log = new AttendanceLog();
            log.setMeeting(meeting);
            log.setUser(user);
            if (action.equalsIgnoreCase("join")) {
                log.setJoinAt(DateTimeUtils.now());
            } else {
                if (action.equalsIgnoreCase("leave")){
                    log.setLeaveAt(DateTimeUtils.now());
                }
            }
            log.setDeviceInfo(request.getHeader("User-Agent"));
            log.setIpAddress(userSessionService.getClientIp(request));
            log.setLocation(userSessionService.getLocationFromIp(userSessionService.getClientIp(request)));

            AttendanceLog savedLog = attendanceLogRepository.save(log);
            return AttendanceLogResponse.mapToResponse(savedLog);
        } catch (Exception e) {
            throw new BadRequestException("Failed to create attendance log: " + e.getMessage());
        }
    }
}