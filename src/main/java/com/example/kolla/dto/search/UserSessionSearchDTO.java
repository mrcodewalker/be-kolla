package com.example.kolla.dto.search;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserSessionSearchDTO extends SearchCriteria {
    private Long userId;
    private String deviceInfo;
    private String ipAddress;
    private String location;
    private String action;
    private Boolean isActive;
}


