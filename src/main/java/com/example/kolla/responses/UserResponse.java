package com.example.kolla.responses;

import com.example.kolla.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String imgUrl;
    private String department;
    private String position;
    private String role;
    private String userCode;
    private boolean isActive;
    private LocalDate dob;
    private String bankName;
    private String bankNumber;
    private String address;
    private String phoneNumber;
    private String degree;
    public static UserResponse mapToResponse(User user){
        return UserResponse.builder()
                .address(user.getAddress())
                .dob(user.getDob())
                .id(user.getId())
                .degree(user.getDegree().toString())
                .bankName(user.getBankName())
                .bankName(user.getBankName())
                .department(user.getDepartment().getDepartmentName())
                .email(user.getEmail())
                .isActive(user.isActive())
                .imgUrl(user.getImgUrl())
                .userCode(user.getUserCode())
                .position(user.getEmail())
                .role(user.getRole().getName())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .build();
    }
}
