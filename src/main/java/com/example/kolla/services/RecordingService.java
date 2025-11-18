package com.example.kolla.services;

import com.example.kolla.dto.RecordingDTO;
import com.example.kolla.dto.search.SearchCriteria;
import com.example.kolla.responses.RecordingResponse;
import com.example.kolla.responses.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface RecordingService {
    RecordingResponse startRecording(Long meetingId, Long userId);

    RecordingResponse stopRecording(Long recordingId);

    RecordingResponse uploadRecording(Long meetingId, Long userId, MultipartFile file) throws IOException;

    RecordingResponse getRecordingById(Long id);

    PageResponse<RecordingResponse> getRecordingsByMeeting(Long meetingId, int page, int size);

    PageResponse<RecordingResponse> searchRecordings(SearchCriteria criteria);

    byte[] downloadRecording(Long id);

    void deleteRecording(Long id);
}