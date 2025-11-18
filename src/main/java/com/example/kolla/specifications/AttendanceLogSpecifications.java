package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.AttendanceLog;
import com.example.kolla.dto.search.AttendanceLogSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class AttendanceLogSpecifications {
    
    public static Specification<AttendanceLog> withSearchCriteria(AttendanceLogSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            // Join fetch để tránh N+1 query
            if (!query.getResultType().equals(Long.class)) {
                root.fetch("meeting", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }
            
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (deviceInfo, ipAddress, location, userName, meetingTitle)
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate deviceInfoPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("deviceInfo")), keyword
                );
                Predicate ipAddressPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("ipAddress")), keyword
                );
                Predicate locationPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")), keyword
                );
                Predicate userNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("name")), keyword
                );
                Predicate meetingTitlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("meeting").get("title")), keyword
                );
                predicates.add(criteriaBuilder.or(deviceInfoPredicate, ipAddressPredicate, locationPredicate, 
                    userNamePredicate, meetingTitlePredicate));
            }

            // Tìm kiếm theo meetingId
            if (searchDTO.getMeetingId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("meeting").get("id"),
                    searchDTO.getMeetingId()
                ));
            }

            // Tìm kiếm theo userId
            if (searchDTO.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("user").get("id"),
                    searchDTO.getUserId()
                ));
            }

            // Tìm kiếm theo startDate (joinAt >= startDate)
            if (searchDTO.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("joinAt"),
                    searchDTO.getStartDate()
                ));
            }

            // Tìm kiếm theo endDate (joinAt <= endDate)
            if (searchDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("joinAt"),
                    searchDTO.getEndDate()
                ));
            }

            // Tìm kiếm theo isPresent
            if (searchDTO.getIsPresent() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isPresent"),
                    searchDTO.getIsPresent()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
