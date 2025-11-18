package com.example.kolla.dto.search;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SearchCriteria {
    private String keyword;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}