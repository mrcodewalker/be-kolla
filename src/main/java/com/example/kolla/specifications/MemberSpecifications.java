package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.Member;
import com.example.kolla.dto.search.MemberSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class MemberSpecifications {
    
    public static Specification<Member> withSearchCriteria(MemberSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (tên user, email, tiêu đề meeting)
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate userPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("name")), keyword
                );
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("email")), keyword
                );
                Predicate meetingTitlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("meeting").get("title")), keyword
                );
                predicates.add(criteriaBuilder.or(userPredicate, emailPredicate, meetingTitlePredicate));
            }

            // Tìm kiếm theo userId
            if (searchDTO.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("user").get("id"),
                    searchDTO.getUserId()
                ));
            }

            // Tìm kiếm theo meetingId
            if (searchDTO.getMeetingId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("meeting").get("id"),
                    searchDTO.getMeetingId()
                ));
            }

            // Tìm kiếm theo roleId
            if (searchDTO.getRoleId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("role").get("id"),
                    searchDTO.getRoleId()
                ));
            }

            // Tìm kiếm theo trạng thái active
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
