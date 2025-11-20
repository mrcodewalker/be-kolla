package com.example.kolla.services.impl;

import com.example.kolla.dto.RecordingDTO;
import com.example.kolla.dto.search.SearchCriteria;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.Recording;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.User;
import com.example.kolla.repositories.RecordingRepository;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.responses.RecordingResponse;
import com.example.kolla.services.RecordingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import com.example.kolla.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordingServiceImpl implements RecordingService {

    private final RecordingRepository recordingRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public RecordingResponse startRecording(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));
            
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));


        Recording recording = new Recording();
        recording.setMeeting(meeting);
        recording.setCreatedBy(user);
        recording.setCreatedAt(DateTimeUtils.now());

        Recording savedRecording = recordingRepository.save(recording);
        return RecordingResponse.mapToResponse(savedRecording);
    }

    @Override
    @Transactional
    public RecordingResponse stopRecording(Long recordingId) {
        Recording recording = recordingRepository.findById(recordingId)
            .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + recordingId));

        recording.setEndTime(DateTimeUtils.now());
        Recording updatedRecording = recordingRepository.save(recording);
        return RecordingResponse.mapToResponse(updatedRecording);
    }

    @Override
    @Transactional
    public RecordingResponse uploadRecording(Long meetingId, Long userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Recording file cannot be empty");
        }

        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));
            
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        Recording recording = new Recording();
        recording.setMeeting(meeting);
        recording.setCreatedBy(user);
        recording.setFileName(fileName);
        recording.setFileSize(file.getSize());
        recording.setFileContent(file.getBytes());
        recording.setStartTime(meeting.getStartTime());
        recording.setEndTime(meeting.getEndTime());
        recording.setCreatedAt(DateTimeUtils.now());

        Recording savedRecording = recordingRepository.save(recording);
        return RecordingResponse.mapToResponse(savedRecording);
    }

    @Override
    public RecordingResponse getRecordingById(Long id) {
        Recording recording = recordingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        return RecordingResponse.mapToResponse(recording);
    }

    @Override
    public PageResponse<RecordingResponse> getRecordingsByMeeting(Long meetingId, int page, int size) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Meeting not found with id: " + meetingId);
        }

        Page<Recording> recordingPage = recordingRepository.findByMeetingId(
            meetingId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return createPageResponse(recordingPage);
    }

    @Override
    public PageResponse<RecordingResponse> searchRecordings(SearchCriteria criteria) {
        Specification<Recording> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getKeyword() != null) {
                String keyword = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("fileName")), keyword));
            }
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), criteria.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(
            criteria.getPage() != null ? criteria.getPage() : 0,
            criteria.getSize() != null ? criteria.getSize() : 10,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Recording> recordingPage = recordingRepository.findAll(spec, pageRequest);
        return createPageResponse(recordingPage);
    }

    @Override
    public byte[] downloadRecording(Long id) {
        Recording recording = recordingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recording not found with id: " + id));
        
        if (recording.getFileContent() == null) {
            throw new BadRequestException("Recording file content is not available");
        }
        
        return recording.getFileContent();
    }

    @Override
    @Transactional
    public void deleteRecording(Long id) {
        if (!recordingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recording not found with id: " + id);
        }
        recordingRepository.deleteById(id);
    }

    private PageResponse<RecordingResponse> createPageResponse(Page<Recording> recordingPage) {
        PageResponse<RecordingResponse> response = new PageResponse<>();
        response.setContent(recordingPage.getContent().stream()
            .map(RecordingResponse::mapToResponse)
            .toList());
        response.setPageNumber(recordingPage.getNumber());
        response.setPageSize(recordingPage.getSize());
        response.setTotalElements(recordingPage.getTotalElements());
        response.setTotalPages(recordingPage.getTotalPages());
        response.setLast(recordingPage.isLast());
        return response;
    }
}