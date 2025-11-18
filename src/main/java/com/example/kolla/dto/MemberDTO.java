package com.example.kolla.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class MemberDTO {
    private Long userId;
    private Long roomId;
}
