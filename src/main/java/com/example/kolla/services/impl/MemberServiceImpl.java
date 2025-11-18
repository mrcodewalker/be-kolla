package com.example.kolla.services.impl;

import com.example.kolla.dto.MemberApprovalDTO;
import com.example.kolla.dto.MemberRejectDTO;
import com.example.kolla.dto.MemberUpdateDTO;
import com.example.kolla.dto.search.MemberSearchDTO;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.*;
import com.example.kolla.responses.MeetingAccessResponse;
import com.example.kolla.repositories.*;
import com.example.kolla.responses.MeetingResponse;
import com.example.kolla.responses.MemberListResponse;
import com.example.kolla.responses.MemberResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.UserResponse;
import com.example.kolla.services.MemberService;
import com.example.kolla.services.NotificationService;
import com.example.kolla.specifications.MemberSpecifications;
import com.example.kolla.dto.NotificationDTO;
import com.example.kolla.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MeetingRepository meetingRepository;
    private final JwtService jwtService;
    private final NotificationService notificationService;


    public void createMemberRequest(Long userId, Long meetingId, Boolean isActive) {
        boolean active = (isActive != null) ? isActive : false;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        // Block duplicate requests: any existing membership (pending or approved)
        if (memberRepository.existsByUserIdAndMeetingId(userId, meetingId)) {
            throw new BadRequestException("Member request already exists for this meeting");
        }

        Member member = new Member();
        member.setUser(user);
        member.setMeeting(meeting);
        member.setActive(active);

        Role defaultRole = roleRepository.findByName("USER");
        if (defaultRole == null) {
            throw new ResourceNotFoundException("Default USER role not found");
        }
        member.setRole(defaultRole);

        try {
            memberRepository.save(member);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Handle race conditions hitting unique constraint
            throw new BadRequestException("Member request already exists for this meeting");
        }
    }

    @Override
    public void approveMember(MemberApprovalDTO memberApprovalDTO) {
        // Authorization: allow if global ADMIN/SECRETARY or meeting-scoped ADMIN/SECRETARY
        User current = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hasAdminOrSecretaryPrivilege(current, memberApprovalDTO.getMeetingId())) {
            throw new BadRequestException("Insufficient privileges to approve members");
        }
        // Lấy tất cả member IDs và role IDs theo từng member
        List<Long> memberIds = memberApprovalDTO.getApprovals().stream()
                .map(MemberApprovalDTO.ApprovalItem::getMemberId)
                .collect(Collectors.toList());
        
        // Lấy meetingId
        Long meetingId = memberApprovalDTO.getMeetingId();
        
        // Tìm tất cả members trong một lần query
        List<Member> members = memberRepository.findAllById(memberIds);
        if (members.size() != memberIds.size()) {
            throw new ResourceNotFoundException("Some members were not found");
        }
        
        // Kiểm tra tất cả members thuộc cùng meeting
        boolean allMembersInSameMeeting = members.stream()
                .allMatch(member -> member.getMeeting() != null && member.getMeeting().getId().equals(meetingId));
        if (!allMembersInSameMeeting) {
            throw new BadRequestException("All members must belong to the same meeting");
        }

        // Tạo map memberId -> roleId
        java.util.Map<Long, Long> memberToRole = memberApprovalDTO.getApprovals().stream()
                .collect(Collectors.toMap(MemberApprovalDTO.ApprovalItem::getMemberId, MemberApprovalDTO.ApprovalItem::getRoleId));

        // Tải tất cả roles cần thiết và tạo map roleId -> Role
        java.util.Set<Long> roleIds = new java.util.HashSet<>(memberToRole.values());
        List<Role> roles = roleRepository.findAllById(roleIds);
        java.util.Map<Long, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getId, r -> r));
        if (roleMap.size() != roleIds.size()) {
            throw new ResourceNotFoundException("Some roles were not found");
        }

        // Validate: Chỉ cho phép 1 ADMIN trong một meeting
        // Kiểm tra xem có member nào đang được assign ADMIN role không
        long adminCountInApproval = members.stream()
                .filter(member -> {
                    Long roleId = memberToRole.get(member.getId());
                    Role role = roleMap.get(roleId);
                    return role != null && "ADMIN".equalsIgnoreCase(role.getName());
                })
                .count();
        
        if (adminCountInApproval > 0) {
            // Đếm số ADMIN hiện tại trong meeting (active), không tính các member đang được approve
            long currentAdminCount = memberRepository.countAdminMembersByMeetingId(meetingId);
            
            // Nếu đã có ADMIN và đang cố gắng assign ADMIN cho member khác
            if (currentAdminCount > 0) {
                // Kiểm tra xem có phải đang update member hiện tại (đã là ADMIN) không
                boolean isUpdatingExistingAdmin = members.stream()
                        .filter(member -> {
                            Long roleId = memberToRole.get(member.getId());
                            Role role = roleMap.get(roleId);
                            return role != null && "ADMIN".equalsIgnoreCase(role.getName());
                        })
                        .anyMatch(member -> {
                            // Member này đã là ADMIN trước đó
                            return member.getRole() != null 
                                    && "ADMIN".equalsIgnoreCase(member.getRole().getName())
                                    && member.isActive();
                        });
                
                if (!isUpdatingExistingAdmin) {
                    throw new BadRequestException("Một meeting chỉ có thể có 1 member với quyền ADMIN");
                }
            }
            
            // Nếu đang assign nhiều ADMIN trong cùng một lần approve
            if (adminCountInApproval > 1) {
                throw new BadRequestException("Một meeting chỉ có thể có 1 member với quyền ADMIN");
            }
        }

        // Cập nhật tất cả members
        members.forEach(member -> {
            Long roleId = memberToRole.get(member.getId());
            Role role = roleMap.get(roleId);
            member.setRole(role);
            member.setActive(true);
        });

        // Lưu tất cả trong một lần
        memberRepository.saveAll(members);
    }

    @Override
    public void rejectMembers(MemberRejectDTO rejectDTO) {
        List<Long> memberIds = rejectDTO.getRejects().stream()
                .map(MemberRejectDTO.RejectInfo::getMemberId)
                .collect(Collectors.toList());
        Long meetingId = rejectDTO.getMeetingId();

        // Get current user who is rejecting
        User current = null;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                current = (User) principal;
            }
        } catch (Exception ignored) {}

        // Tìm tất cả members trong một lần query
        List<Member> members = memberRepository.findAllById(memberIds);

        // Kiểm tra xem có member nào không tồn tại
        if (members.size() != memberIds.size()) {
            throw new ResourceNotFoundException("Some members were not found");
        }

        // Kiểm tra tất cả members thuộc cùng meeting
        boolean allMembersInSameMeeting = members.stream()
                .allMatch(member -> member.getMeeting() != null && member.getMeeting().getId().equals(meetingId));
        if (!allMembersInSameMeeting) {
            throw new BadRequestException("All members must belong to the same meeting");
        }

        // Get meeting info for notification
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        // Create notifications and delete members
        for (Member member : members) {
            User targetUser = member.getUser();
            if (targetUser == null) {
                continue;
            }
            String rejecterName = (current != null && current.getName() != null) ? current.getName() : "Quản trị viên";
            
            // Create notification for rejected user
            NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setTitle("Bạn đã bị xóa khỏi phòng họp");
            notificationDTO.setContent(String.format("Bạn đã bị xóa khỏi phòng họp: %s bởi %s", 
                    meeting.getTitle(), rejecterName));
            notificationDTO.setNotificationType(NotificationType.MEETING_CANCELLED);
            Long senderId = current != null ? current.getId()
                    : (meeting.getCreatedBy() != null ? meeting.getCreatedBy().getId() : null);
            if (senderId != null) {
                notificationDTO.setSenderId(senderId);
                notificationDTO.setReceiverId(targetUser.getId()); // Người nhận notification (bị reject)
                notificationDTO.setMeetingId(meeting.getId());
                notificationService.createNotification(notificationDTO);
            }
        }

        members.forEach(member -> member.setActive(false));
        memberRepository.deleteAll(members);
    }

    @Override
    public MemberResponse updateMember(Long memberId, MemberUpdateDTO memberUpdateDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        // Lưu role cũ để kiểm tra xem có thay đổi không
        Role oldRole = member.getRole();
        String oldRoleName = oldRole != null ? oldRole.getName() : null;
        
        if (memberUpdateDTO.getRoleId() != null) {
            Role role = roleRepository.findById(memberUpdateDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            
            // Validate: Chỉ cho phép 1 ADMIN trong một meeting
            if ("ADMIN".equalsIgnoreCase(role.getName())) {
                Long meetingId = member.getMeeting() != null ? member.getMeeting().getId() : null;
                if (meetingId != null) {
                    long currentAdminCount = memberRepository.countAdminMembersByMeetingId(meetingId);
                    
                    // Nếu đã có ADMIN và member hiện tại không phải là ADMIN
                    if (currentAdminCount > 0) {
                        boolean isCurrentMemberAdmin = member.getRole() != null 
                                && "ADMIN".equalsIgnoreCase(member.getRole().getName())
                                && member.isActive();
                        
                        if (!isCurrentMemberAdmin) {
                            throw new BadRequestException("Một meeting chỉ có thể có 1 member với quyền ADMIN");
                        }
                    }
                }
            }
            
            member.setRole(role);
        }
        
        if (memberUpdateDTO.getIsActive() != null) {
            member.setActive(memberUpdateDTO.getIsActive());
        }
        
        member = memberRepository.save(member);
        
        // Tạo notification nếu role đã thay đổi
        if (memberUpdateDTO.getRoleId() != null && member.getRole() != null) {
            String newRoleName = member.getRole().getName();
            if (oldRoleName == null || !oldRoleName.equals(newRoleName)) {
                // Lấy thông tin user hiện tại (người update)
                User current = null;
                try {
                    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    if (principal instanceof User) {
                        current = (User) principal;
                    }
                } catch (Exception ignored) {}
                
                // Lấy thông tin user của member (người nhận notification)
                User memberUser = member.getUser();
                Meeting meeting = member.getMeeting();
                
                if (memberUser != null && meeting != null) {
                    String updaterName = (current != null && current.getName() != null) ? current.getName() : "Quản trị viên";
                    NotificationDTO notificationDTO = new NotificationDTO();
                    notificationDTO.setTitle("Vai trò của bạn đã được cập nhật");
                    notificationDTO.setContent(String.format("Vai trò của bạn trong cuộc họp '%s' đã được cập nhật từ '%s' thành '%s' bởi %s", 
                            meeting.getTitle(), 
                            oldRoleName != null ? oldRoleName : "chưa có", 
                            newRoleName, 
                            updaterName));
                    notificationDTO.setNotificationType(NotificationType.ROLE_CHANGED);
                    
                    Long senderId = current != null ? current.getId()
                            : (meeting.getCreatedBy() != null ? meeting.getCreatedBy().getId() : null);
                    if (senderId != null) {
                        notificationDTO.setSenderId(senderId);
                        notificationDTO.setReceiverId(memberUser.getId());
                        notificationDTO.setMeetingId(meeting.getId());
                        notificationService.createNotification(notificationDTO);
                    }
                }
            }
        }
        
        return MemberResponse.mapToResponse(member);
    }

    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        memberRepository.delete(member);
    }

    @Override
    public void deleteMemberByUserAndMeeting(Long userId, Long meetingId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        memberRepository.deleteByUserIdAndMeetingId(userId, meetingId);
    }

    @Override
    public void deleteMemberByUserAndRoom(Long userId, Long roomId) {
        memberRepository.deleteByUserIdAndMeetingRoomId(userId, roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        return MemberResponse.mapToResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberListResponse> searchMembers(MemberSearchDTO searchDTO) {
        // Authorization: if searching within meeting, allow if user is ADMIN/SECRETARY (global or meeting-scoped) or is a member of the meeting
        User current = null;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                current = (User) principal;
            }
        } catch (Exception ignored) {}
        
        Long meetingIdForAuth = searchDTO.getMeetingId();
        if (meetingIdForAuth != null && current != null) {
            // Cho phép nếu là global ADMIN/SECRETARY hoặc ADMIN/SECRETARY trong meeting
            boolean hasPrivilege = hasAdminOrSecretaryPrivilege(current, meetingIdForAuth);
            
            // Nếu không có privilege, kiểm tra xem user có phải là member của meeting không
            if (!hasPrivilege) {
                boolean isMember = memberRepository.existsByUserIdAndMeetingIdAndIsActive(
                    current.getId(), meetingIdForAuth, true);
                if (!isMember) {
                    throw new BadRequestException("Insufficient privileges to search members for meeting");
                }
            }
        }

        // Tạo specification từ search criteria
        Specification<Member> spec = MemberSpecifications.withSearchCriteria(searchDTO);
        
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
        Page<Member> memberPage = memberRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang response
        List<MemberResponse> memberResponses = memberPage.getContent().stream()
                .map(MemberResponse::mapToResponse)
                .collect(Collectors.toList());

        // Lấy meetingId từ điều kiện tìm kiếm (nếu có)
        Long meetingIdFilter = searchDTO.getMeetingId();
        
        // Tạo MemberListResponse
        MemberListResponse memberListResponse = MemberListResponse.builder()
                .members(memberResponses)
                .pendingCount(meetingIdFilter != null ? memberRepository.countByMeetingIdAndIsActive(meetingIdFilter, false) : null)
                .joinedCount(meetingIdFilter != null ? memberRepository.countByMeetingIdAndIsActive(meetingIdFilter, true) : null)
                .build();
        
        // Tạo PageResponse
        PageResponse<MemberListResponse> response = new PageResponse<>();
        response.setContent(List.of(memberListResponse));
        response.setPageNumber(memberPage.getNumber());
        response.setPageSize(memberPage.getSize());
        response.setTotalElements(memberPage.getTotalElements());
        response.setTotalPages(memberPage.getTotalPages());
        response.setLast(memberPage.isLast());
        return response;
    }

    private boolean hasAdminOrSecretaryPrivilege(User user, Long meetingId) {
        if (user == null) return false;
        if (user.getRole() != null && user.getRole().getName() != null) {
            String rn = user.getRole().getName();
            if ("ADMIN".equalsIgnoreCase(rn) || "SECRETARY".equalsIgnoreCase(rn)) {
                return true;
            }
        }
        if (meetingId == null) return false;
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) return false;

        boolean isActiveMember = memberRepository.existsByUserIdAndMeetingIdAndIsActive(user.getId(), meeting.getId(), true);
        if (!isActiveMember) return false;
        return memberRepository.findByUserIdAndMeetingId(user.getId(), meeting.getId())
                .map(m -> m.getRole() != null && m.getRole().getName() != null
                        && ("ADMIN".equalsIgnoreCase(m.getRole().getName()) || "SECRETARY".equalsIgnoreCase(m.getRole().getName())))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMemberOfMeeting(Long userId, Long meetingId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        return memberRepository.existsByUserIdAndMeetingIdAndIsActive(userId, meetingId, true);
    }

    @Override
    public boolean isUserMemberOfRoom(Long userId, Long roomId) {
        return memberRepository.existsByUserIdAndMeetingRoomIdAndIsActive(userId, roomId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getUserActiveStatusInMeeting(Long userId, Long meetingId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        return memberRepository.findByUserIdAndMeetingId(userId, meetingId)
                .map(Member::isActive)
                .orElse(null); // Trả về null nếu user không phải member của meeting
    }

    @Override
    public Boolean getUserActiveStatusInRoom(Long userId, Long roomId) {
        return memberRepository.findByUserIdAndMeetingRoomId(userId, roomId)
                .map(Member::isActive)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingMembersByMeetingId(Long meetingId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        return memberRepository.countByMeetingIdAndIsActive(meetingId, false);
    }

    @Override
    public Long countPendingMembersByRoomId(Long roomId) {
        return memberRepository.countByMeetingRoomIdAndIsActive(roomId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countJoinedMembersByMeetingId(Long meetingId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
        return memberRepository.countByMeetingIdAndIsActive(meetingId, true);
    }

    @Override
    public Long countJoinedMembersByRoomId(Long roomId) {
        return memberRepository.countByMeetingRoomIdAndIsActive(roomId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingAccessResponse checkMeetingAccess(String token, String meetLink) {
        try {
            // Tìm meeting từ meetLink
            Meeting meeting = meetingRepository.findByMeetingLink(meetLink)
                    .orElse(null);
            if (meeting == null) {
                return MeetingAccessResponse.builder()
                    .hasAccess(false)
                    .build();
            }

            // Kiểm tra xem meeting có member active không
            boolean hasActiveMembers = memberRepository.existsByMeetingIdAndIsActive(meeting.getId(), true);
            if (!hasActiveMembers) {
                return MeetingAccessResponse.builder()
                    .hasAccess(false)
                    .build();
            }

            // Nếu có token, kiểm tra quyền truy cập của user
            if (token != null && !token.isEmpty()) {
                try {
                    String userEmail = jwtService.extractUsername(token);
                    if (userEmail != null) {
                        User user = userRepository.findByEmail(userEmail)
                                .orElse(null);
                        if (user != null) {
                            boolean isMember = memberRepository.existsByUserIdAndMeetingIdAndIsActive(user.getId(), meeting.getId(), true);
                            String meetingRole = null;
                            String membershipStatus = null;
                            if (isMember) {
                                membershipStatus = "APPROVED";
                                meetingRole = memberRepository.findByUserIdAndMeetingId(user.getId(), meeting.getId())
                                        .map(m -> m.getRole() != null ? m.getRole().getName() : null)
                                        .orElse(null);
                            } else {
                                membershipStatus = "NONE";
                            }
                            return MeetingAccessResponse.builder()
                                .hasAccess(isMember)
                                .meeting(MeetingResponse.mapToResponse(meeting, membershipStatus, meetingRole))
                                .user(UserResponse.mapToResponse(user))
                                .build();
                        }
                    }
                } catch (Exception e) {
                    // Token không hợp lệ, trả về không có quyền truy cập
                    return MeetingAccessResponse.builder()
                        .hasAccess(false)
                        .build();
                }
            }

            // Nếu không có token, trả về không có quyền truy cập
            return MeetingAccessResponse.builder()
                .hasAccess(false)
                .build();

        } catch (Exception e) {
            return MeetingAccessResponse.builder()
                .hasAccess(false)
                .build();
        }
    }

    @Override
    @Transactional
    public void inviteMembers(com.example.kolla.dto.MemberInviteDTO inviteDTO) {
        Long meetingId = inviteDTO.getMeetingId();
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        // Authorization: allow if global ADMIN/SECRETARY or meeting-scoped ADMIN/SECRETARY
        User current = null;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                current = (User) principal;
            }
        } catch (Exception ignored) {}
        if (!hasAdminOrSecretaryPrivilege(current, meetingId)) {
            throw new BadRequestException("Insufficient privileges to invite members");
        }

        // Preload all roles used
        java.util.Set<Long> roleIds = inviteDTO.getMembers().stream()
                .map(com.example.kolla.dto.MemberInviteDTO.InviteItem::getRoleId)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, Role> roleMap = roleRepository.findAllById(roleIds).stream()
                .collect(java.util.stream.Collectors.toMap(Role::getId, r -> r));
        if (roleMap.size() != roleIds.size()) {
            throw new ResourceNotFoundException("Some roles were not found");
        }

        // Validate: Chỉ cho phép 1 ADMIN trong một meeting
        // Kiểm tra xem có member nào đang được assign ADMIN role không
        long adminCountInInvite = inviteDTO.getMembers().stream()
                .filter(item -> {
                    Role role = roleMap.get(item.getRoleId());
                    return role != null && "ADMIN".equalsIgnoreCase(role.getName());
                })
                .count();
        
        if (adminCountInInvite > 0) {
            // Đếm số ADMIN hiện tại trong meeting (active)
            long currentAdminCount = memberRepository.countAdminMembersByMeetingId(meetingId);
            
            // Nếu đã có ADMIN và đang cố gắng assign ADMIN cho member khác
            if (currentAdminCount > 0) {
                // Kiểm tra xem có phải đang update member hiện tại (đã là ADMIN) không
                boolean isUpdatingExistingAdmin = inviteDTO.getMembers().stream()
                        .filter(item -> {
                            Role role = roleMap.get(item.getRoleId());
                            return role != null && "ADMIN".equalsIgnoreCase(role.getName());
                        })
                        .anyMatch(item -> {
                            java.util.Optional<Member> existingMember = memberRepository.findByUserIdAndMeetingId(item.getUserId(), meetingId);
                            return existingMember.isPresent() 
                                    && existingMember.get().getRole() != null 
                                    && "ADMIN".equalsIgnoreCase(existingMember.get().getRole().getName())
                                    && existingMember.get().isActive();
                        });
                
                if (!isUpdatingExistingAdmin) {
                    throw new BadRequestException("Một meeting chỉ có thể có 1 member với quyền ADMIN");
                }
            }
            
            // Nếu đang assign nhiều ADMIN trong cùng một lần invite
            if (adminCountInInvite > 1) {
                throw new BadRequestException("Một meeting chỉ có thể có 1 member với quyền ADMIN");
            }
        }

        // Process invitations
        for (com.example.kolla.dto.MemberInviteDTO.InviteItem item : inviteDTO.getMembers()) {
            Long userId = item.getUserId();
            Long roleId = item.getRoleId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            Role role = roleMap.get(roleId);

            Member member = memberRepository.findByUserIdAndMeetingId(user.getId(), meeting.getId())
                    .orElse(null);
            if (member == null) {
                member = new Member();
                member.setUser(user);
                member.setMeeting(meeting);
            }
            member.setRole(role);
            member.setActive(true);
            memberRepository.save(member);

            // Create notification for invited user
            String inviterName = (current != null && current.getName() != null) ? current.getName() : "Quản trị viên";
            NotificationDTO notificationDTO = new NotificationDTO();
            notificationDTO.setTitle("Bạn đã được mời tham gia cuộc họp");
            notificationDTO.setContent(String.format("Bạn đã được mời tham gia cuộc họp: %s. Vai trò: %s bởi %s", 
                    meeting.getTitle(), role.getName(), inviterName));
            notificationDTO.setNotificationType(NotificationType.MEETING_CREATED);
            Long senderId = current != null ? current.getId()
                    : (meeting.getCreatedBy() != null ? meeting.getCreatedBy().getId() : null);
            if (senderId != null) {
                notificationDTO.setSenderId(senderId);
                notificationDTO.setReceiverId(user.getId()); // Người nhận notification (được invite)
                notificationDTO.setMeetingId(meeting.getId());
                notificationService.createNotification(notificationDTO);
            }
        }
    }
}
