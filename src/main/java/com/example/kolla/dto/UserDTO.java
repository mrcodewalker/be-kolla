package com.example.kolla.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String imgUrl;
    private String position;
    private String userCode;
    private boolean isActive;
    private LocalDate dob;
    private String bankName;
    private String bankNumber;
    private String address;
    private String phoneNumber;
    private Long departmentId;
    private Long roleId;
    private String degree;
}