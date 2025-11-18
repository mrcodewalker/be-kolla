package com.example.kolla.services;

import com.example.kolla.dto.MessageCreateDTO;
import com.example.kolla.dto.search.MeetingMessageSearchDTO;
import com.example.kolla.responses.MeetingMessageResponse;
import com.example.kolla.responses.PageResponse;

public interface MeetingMessageService {
    MeetingMessageResponse createMessage(MessageCreateDTO createDTO);
    MeetingMessageResponse updateMessage(Long messageId, String content);
    MeetingMessageResponse getMessageById(Long id);
    PageResponse<MeetingMessageResponse> getMessagesByMeeting(Long meetingId, int page, int size);
    PageResponse<MeetingMessageResponse> getPrivateMessages(Long meetingId, Long userId, Long otherUserId, int page, int size);
    PageResponse<MeetingMessageResponse> searchMessages(MeetingMessageSearchDTO searchDTO);
    void deleteMessage(Long id);
    void deleteAllMessages(Long meetingId);
    boolean isMessageEditable(Long messageId, Long userId);
}