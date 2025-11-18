package com.example.kolla.dto;

import java.time.LocalDate;

import com.example.kolla.enums.Degree;
import lombok.Data;

@Data
public class UserUpdateDTO {
    private String email;
    private String fullName;
    private String phoneNumber;
    private String imgUrl;
    private Long departmentId;
    private Long roleId;
    private Degree degree;
    private Boolean isActive;
    private LocalDate dob;
    private String identification;
    private String address;
    private String bankName;
    private String bankNumber;
}