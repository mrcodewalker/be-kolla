package com.example.kolla.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberListResponse {
    private List<MemberResponse> members;
    private Long pendingCount;
    private Long joinedCount;
}

