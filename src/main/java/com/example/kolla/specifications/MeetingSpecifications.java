package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.Meeting;
import com.example.kolla.dto.search.MeetingSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class MeetingSpecifications {
    
    public static Specification<Meeting> withSearchCriteria(MeetingSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (title, description)
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), keyword
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), keyword
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            // Tìm kiếm theo title
            if (searchDTO.getTitle() != null && !searchDTO.getTitle().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + searchDTO.getTitle().toLowerCase() + "%"
                ));
            }

            // Tìm kiếm theo description
            if (searchDTO.getDescription() != null && !searchDTO.getDescription().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + searchDTO.getDescription().toLowerCase() + "%"
                ));
            }

            // Tìm kiếm theo createdBy
            if (searchDTO.getCreatedBy() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("createdBy").get("id"),
                    searchDTO.getCreatedBy()
                ));
            }

            // Tìm kiếm theo roomId (thêm nếu chưa có trong MeetingSearchDTO)
            if (searchDTO.getRoomId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("room").get("id"),
                    searchDTO.getRoomId()
                ));
            }

            // Tìm kiếm theo startTime (meeting.startTime >= startTime)
            if (searchDTO.getStartTime() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("startTime"),
                    searchDTO.getStartTime()
                ));
            }

            // Tìm kiếm theo endTime (meeting.endTime <= endTime)
            if (searchDTO.getEndTime() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("endTime"),
                    searchDTO.getEndTime()
                ));
            }

            // Tìm kiếm theo startDate từ SearchCriteria (meeting.startTime >= startDate)
            if (searchDTO.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("startTime"),
                    searchDTO.getStartDate()
                ));
            }

            // Tìm kiếm theo endDate từ SearchCriteria (meeting.endTime <= endDate)
            if (searchDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("endTime"),
                    searchDTO.getEndDate()
                ));
            }

            // Tìm kiếm theo isRecording (nếu có field này trong model)
            if (searchDTO.getIsRecording() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isMeeting"),
                    searchDTO.getIsRecording()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

