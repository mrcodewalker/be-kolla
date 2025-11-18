package com.example.kolla.services.impl;

import com.example.kolla.dto.MessageCreateDTO;
import com.example.kolla.dto.search.MeetingMessageSearchDTO;
import com.example.kolla.responses.MeetingMessageResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.specifications.MeetingMessageSpecifications;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.*;
import com.example.kolla.repositories.MeetingMessageRepository;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.services.MeetingMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingMessageServiceImpl implements MeetingMessageService {

    private final MeetingMessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final com.example.kolla.services.impl.JwtService jwtService;

    @Override
    @Transactional
    public MeetingMessageResponse createMessage(MessageCreateDTO createDTO) {
        try {
            // Tìm meeting bằng meeting link
            Meeting meeting = meetingRepository.findByMeetingLink(createDTO.getMeetLink())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with link: " + createDTO.getMeetLink()));

            // Kiểm tra và lấy thông tin user từ token
            String userEmail = jwtService.extractUsername(createDTO.getToken());
            if (userEmail == null) {
                throw new BadRequestException("Invalid token");
            }

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            // Tạo message mới
            MeetingMessage message = new MeetingMessage();
            message.setMessage(createDTO.getMessage());
            message.setMeeting(meeting);
            message.setSentAt(LocalDateTime.now());
            message.setSender(user);

            MeetingMessage savedMessage = messageRepository.save(message);
            return MeetingMessageResponse.mapToResponse(savedMessage);
        } catch (Exception e) {
            throw new BadRequestException("Failed to create message: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public MeetingMessageResponse updateMessage(Long messageId, String content) {
        MeetingMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be empty");
        }

        message.setMessage(content);

        MeetingMessage updatedMessage = messageRepository.save(message);
        return MeetingMessageResponse.mapToResponse(updatedMessage);
    }

    @Override
    public MeetingMessageResponse getMessageById(Long id) {
        MeetingMessage message = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        return MeetingMessageResponse.mapToResponse(message);
    }

    @Override
    public PageResponse<MeetingMessageResponse> getMessagesByMeeting(Long meetingId, int page, int size) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Meeting not found with id: " + meetingId);
        }

        // Create specification to get:
        // 1. All public messages (receiver is null)
        // 2. Private messages where current user is either sender or receiver
        Specification<MeetingMessage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Meeting ID condition
            predicates.add(cb.equal(root.get("meeting").get("id"), meetingId));
            
            // Message visibility condition
            predicates.add(cb.isNull(root.get("receiver")));  // Public messages
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MeetingMessage> messagePage = messageRepository.findAll(
            spec,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        return createPageResponse(messagePage);
    }

    @Override
    public PageResponse<MeetingMessageResponse> getPrivateMessages(Long meetingId, Long userId, Long otherUserId, int page, int size) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Meeting not found with id: " + meetingId);
        }

        // Create specification to get private messages between two users
        Specification<MeetingMessage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Meeting ID condition
            predicates.add(cb.equal(root.get("meeting").get("id"), meetingId));
            
            // Private messages between users condition
            Predicate userAToUserB = cb.and(
                cb.equal(root.get("sender").get("id"), userId),
                cb.equal(root.get("receiver").get("id"), otherUserId)
            );
            
            Predicate userBToUserA = cb.and(
                cb.equal(root.get("sender").get("id"), otherUserId),
                cb.equal(root.get("receiver").get("id"), userId)
            );
            
            predicates.add(cb.or(userAToUserB, userBToUserA));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MeetingMessage> messagePage = messageRepository.findAll(
            spec,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        return createPageResponse(messagePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MeetingMessageResponse> searchMessages(MeetingMessageSearchDTO searchDTO) {
        // Tạo specification từ search criteria
        Specification<MeetingMessage> spec = MeetingMessageSpecifications.withSearchCriteria(searchDTO);
        
        // Tạo sort
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "sentAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        // Tạo pageable
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        PageRequest pageable = PageRequest.of(page, size, sort);
        
        // Thực hiện query
        Page<MeetingMessage> messagePage = messageRepository.findAll(spec, pageable);
        
        return createPageResponse(messagePage);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Message not found with id: " + id);
        }
        messageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllMessages(Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Meeting not found with id: " + meetingId);
        }
        messageRepository.deleteByMeetingId(meetingId);
    }

    @Override
    public boolean isMessageEditable(Long messageId, Long userId) {
        MeetingMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        return message.getSender().getId().equals(userId);
    }

    private PageResponse<MeetingMessageResponse> createPageResponse(Page<MeetingMessage> messagePage) {
        PageResponse<MeetingMessageResponse> response = new PageResponse<>();
        response.setContent(messagePage.getContent().stream()
            .map(MeetingMessageResponse::mapToResponse)
            .toList());
        response.setPageNumber(messagePage.getNumber());
        response.setPageSize(messagePage.getSize());
        response.setTotalElements(messagePage.getTotalElements());
        response.setTotalPages(messagePage.getTotalPages());
        response.setLast(messagePage.isLast());
        return response;
    }
}