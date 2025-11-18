package com.example.kolla.specifications;

import org.springframework.data.jpa.domain.Specification;
import com.example.kolla.models.MeetingMessage;
import com.example.kolla.dto.search.MeetingMessageSearchDTO;
import jakarta.persistence.criteria.Predicate;

public class MeetingMessageSpecifications {
    
    public static Specification<MeetingMessage> withSearchCriteria(MeetingMessageSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // Tìm kiếm theo keyword (message content, sender name)
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String keyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate messagePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("message")), keyword
                );
                Predicate senderNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("sender").get("name")), keyword
                );
                predicates.add(criteriaBuilder.or(messagePredicate, senderNamePredicate));
            }

            // Tìm kiếm theo meetingId
            if (searchDTO.getMeetingId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("meeting").get("id"),
                    searchDTO.getMeetingId()
                ));
            }

            // Tìm kiếm theo senderId
            if (searchDTO.getSenderId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("sender").get("id"),
                    searchDTO.getSenderId()
                ));
            }

            // Note: MeetingMessage model may not have receiver field
            // Uncomment if receiver field exists in the model
            // if (searchDTO.getReceiverId() != null) {
            //     predicates.add(criteriaBuilder.equal(
            //         root.get("receiver").get("id"),
            //         searchDTO.getReceiverId()
            //     ));
            // }

            // Tìm kiếm theo startDate (sentAt >= startDate)
            if (searchDTO.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("sentAt"),
                    searchDTO.getStartDate()
                ));
            }

            // Tìm kiếm theo endDate (sentAt <= endDate)
            if (searchDTO.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("sentAt"),
                    searchDTO.getEndDate()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
