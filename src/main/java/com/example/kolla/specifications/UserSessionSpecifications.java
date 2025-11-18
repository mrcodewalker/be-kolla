package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.UserSession;
import com.example.kolla.dto.search.UserSessionSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class UserSessionSpecifications {
    
    public static Specification<UserSession> withSearchCriteria(UserSessionSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (deviceInfo, ipAddress, location, action, userName, userEmail)
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
                Predicate actionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("action")), keyword
                );
                Predicate userNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("name")), keyword
                );
                Predicate userEmailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("email")), keyword
                );
                predicates.add(criteriaBuilder.or(deviceInfoPredicate, ipAddressPredicate, locationPredicate, 
                    actionPredicate, userNamePredicate, userEmailPredicate));
            }

            // Tìm kiếm theo userId
            if (searchDTO.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("user").get("id"),
                    searchDTO.getUserId()
                ));
            }

            // Tìm kiếm theo deviceInfo
            if (searchDTO.getDeviceInfo() != null && !searchDTO.getDeviceInfo().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("deviceInfo")),
                    "%" + searchDTO.getDeviceInfo().toLowerCase() + "%"
                ));
            }

            // Tìm kiếm theo ipAddress
            if (searchDTO.getIpAddress() != null && !searchDTO.getIpAddress().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("ipAddress")),
                    "%" + searchDTO.getIpAddress().toLowerCase() + "%"
                ));
            }

            // Tìm kiếm theo location
            if (searchDTO.getLocation() != null && !searchDTO.getLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")),
                    "%" + searchDTO.getLocation().toLowerCase() + "%"
                ));
            }

            // Tìm kiếm theo action
            if (searchDTO.getAction() != null && !searchDTO.getAction().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("action")),
                    "%" + searchDTO.getAction().toLowerCase() + "%"
                ));
            }

            // Tìm kiếm theo isActive
            if (searchDTO.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isActive"),
                    searchDTO.getIsActive()
                ));
            }

            // Tìm kiếm theo startDate (createdAt >= startDate)
            if (searchDTO.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    searchDTO.getStartDate()
                ));
            }

            // Tìm kiếm theo endDate (createdAt <= endDate)
            if (searchDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    searchDTO.getEndDate()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


