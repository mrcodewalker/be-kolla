package com.example.kolla.services;

import com.example.kolla.dto.MemberApprovalDTO;
import com.example.kolla.dto.MemberRejectDTO;
import com.example.kolla.dto.MemberUpdateDTO;
import com.example.kolla.dto.search.MemberSearchDTO;
import com.example.kolla.responses.MeetingAccessResponse;
import com.example.kolla.responses.MemberListResponse;
import com.example.kolla.responses.MemberResponse;
import com.example.kolla.responses.PageResponse;


public interface MemberService {

    void createMemberRequest(Long userId, Long meetingId, Boolean isActive);
    
    void approveMember(MemberApprovalDTO memberApprovalDTO);
    
    // Admin/Secretary từ chối member request (xóa record)
    void rejectMembers(MemberRejectDTO rejectDTO);
    
    // Cập nhật member (chỉ admin/secretary)
    MemberResponse updateMember(Long memberId, MemberUpdateDTO memberUpdateDTO);
    
    // Xóa member (user tự xóa hoặc admin/secretary)
    void deleteMember(Long memberId);
    
    // Xóa member theo userId và meetingId
    void deleteMemberByUserAndMeeting(Long userId, Long meetingId);
    // Back-compat: Xóa member theo userId và roomId
    void deleteMemberByUserAndRoom(Long userId, Long roomId);

    // Lấy member theo ID
    MemberResponse getMemberById(Long memberId);
    
    // Tìm kiếm member với criteria và phân trang
    PageResponse<MemberListResponse> searchMembers(MemberSearchDTO searchDTO);
    
    // Kiểm tra user đã là member của meeting chưa
    boolean isUserMemberOfMeeting(Long userId, Long meetingId);
    // Back-compat: Kiểm tra user đã là member của room chưa
    boolean isUserMemberOfRoom(Long userId, Long roomId);
    
    // Lấy trạng thái isActive của user trong meeting
    Boolean getUserActiveStatusInMeeting(Long userId, Long meetingId);
    // Back-compat: Lấy trạng thái isActive của user trong room
    Boolean getUserActiveStatusInRoom(Long userId, Long roomId);
    
    // Đếm số lượng pending members (isActive = false) trong meeting
    Long countPendingMembersByMeetingId(Long meetingId);
    // Back-compat: Đếm pending theo room
    Long countPendingMembersByRoomId(Long roomId);
    
    // Đếm số lượng joined members (isActive = true) trong meeting
    Long countJoinedMembersByMeetingId(Long meetingId);
    // Back-compat: Đếm joined theo room
    Long countJoinedMembersByRoomId(Long roomId);

    // Kiểm tra quyền truy cập vào meeting dựa trên token và meeting link
    MeetingAccessResponse checkMeetingAccess(String token, String meetLink);

    // Mời danh sách user vào meeting với role chỉ định, mặc định active
    void inviteMembers(com.example.kolla.dto.MemberInviteDTO inviteDTO);
}
