package com.example.kolla.dto.search;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentEditLogSearchDTO extends SearchCriteria {
    private Long meetingId;
    private Long editedById;
}

