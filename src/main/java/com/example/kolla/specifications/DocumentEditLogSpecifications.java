package com.example.kolla.specifications;

import com.example.kolla.dto.search.DocumentEditLogSearchDTO;
import com.example.kolla.models.DocumentEditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentEditLogSpecifications {

    private DocumentEditLogSpecifications() {
    }

    public static Specification<DocumentEditLog> withSearchCriteria(DocumentEditLogSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            if (!query.getResultType().equals(Long.class)) {
                root.fetch("meeting", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("editedBy", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isBlank()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate changeSummaryPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("changeSummary")), keyword);
                Predicate meetingTitlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("meeting").get("title")), keyword);
                Predicate editorNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("editedBy").get("name")), keyword);
                predicates.add(criteriaBuilder.or(changeSummaryPredicate, meetingTitlePredicate, editorNamePredicate));
            }

            if (searchDTO.getMeetingId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("meeting").get("id"), searchDTO.getMeetingId())
                );
            }

            if (searchDTO.getEditedById() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("editedBy").get("id"), searchDTO.getEditedById())
                );
            }

            if (searchDTO.getStartDate() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("editedAt"), searchDTO.getStartDate())
                );
            }

            if (searchDTO.getEndDate() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("editedAt"), searchDTO.getEndDate())
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

