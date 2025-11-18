package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.User;
import com.example.kolla.dto.search.UserSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {
    
    public static Specification<User> withSearchCriteria(UserSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (name, email, userCode)
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), keyword
                );
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), keyword
                );
                Predicate userCodePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("userCode")), keyword
                );
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, userCodePredicate));
            }

            if (searchDTO.getName() != null && !searchDTO.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + searchDTO.getName().toLowerCase() + "%"
                ));
            }

            if (searchDTO.getEmail() != null && !searchDTO.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + searchDTO.getEmail().toLowerCase() + "%"
                ));
            }

            if (searchDTO.getUserCode() != null && !searchDTO.getUserCode().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("userCode")),
                    "%" + searchDTO.getUserCode().toLowerCase() + "%"
                ));
            }

            if (searchDTO.getPhoneNumber() != null && !searchDTO.getPhoneNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    root.get("phoneNumber"),
                    "%" + searchDTO.getPhoneNumber() + "%"
                ));
            }

            if (searchDTO.getIdentification() != null && !searchDTO.getIdentification().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    root.get("identification"),
                    "%" + searchDTO.getIdentification() + "%"
                ));
            }

            if (searchDTO.getBankName() != null && !searchDTO.getBankName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("bankName")),
                    "%" + searchDTO.getBankName().toLowerCase() + "%"
                ));
            }

            if (searchDTO.getBankNumber() != null && !searchDTO.getBankNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    root.get("bankNumber"),
                    "%" + searchDTO.getBankNumber() + "%"
                ));
            }

            if (searchDTO.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("department").get("id"),
                    searchDTO.getDepartmentId()
                ));
            }

            if (searchDTO.getRoleId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("role").get("id"),
                    searchDTO.getRoleId()
                ));
            }

            if (searchDTO.getDegree() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("degree"),
                    searchDTO.getDegree()
                ));
            }

            if (searchDTO.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isActive"),
                    searchDTO.getIsActive()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}