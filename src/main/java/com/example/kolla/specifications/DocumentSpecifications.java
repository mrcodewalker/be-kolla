package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.Document;
import com.example.kolla.dto.search.DocumentSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class DocumentSpecifications {
    
    public static Specification<Document> withSearchCriteria(DocumentSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (fileName, userName, meetingTitle)
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate fileNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fileName")), keyword
                );
                Predicate userNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("name")), keyword
                );
                Predicate meetingTitlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("meeting").get("title")), keyword
                );
                predicates.add(criteriaBuilder.or(fileNamePredicate, userNamePredicate, meetingTitlePredicate));
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

            // Tìm kiếm theo fileType
            if (searchDTO.getFileType() != null && !searchDTO.getFileType().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("fileType"),
                    searchDTO.getFileType()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
