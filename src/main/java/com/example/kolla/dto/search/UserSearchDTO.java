package com.example.kolla.dto.search;

import com.example.kolla.enums.Degree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO {
    private String keyword; // Tìm kiếm theo name, email, userCode
    private String name;
    private String email;
    private String userCode;
    private String phoneNumber;
    private String identification;
    private String bankName;
    private String bankNumber;
    private Long departmentId;
    private Long roleId;
    private Degree degree;
    private Boolean isActive;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}