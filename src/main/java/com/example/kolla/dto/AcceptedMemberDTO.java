package com.example.kolla.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class AcceptedMemberDTO {
    private Long id;
    private Long roleId;
}
