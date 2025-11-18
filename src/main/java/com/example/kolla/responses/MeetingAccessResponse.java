package com.example.kolla.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingAccessResponse {
    private boolean hasAccess;
    private MeetingResponse meeting;
    private UserResponse user;
}



