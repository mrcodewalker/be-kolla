package com.example.kolla.specifications;

import com.example.kolla.dto.search.RoomSearchDTO;
import com.example.kolla.models.Room;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class RoomSpecifications {
    public static Specification<Room> withSearchCriteria(RoomSearchDTO searchDTO) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isBlank()) {
                String like = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("roomName")), like);
                Predicate byCode = cb.like(cb.lower(root.get("roomCode")), like);

                var deptJoin = root.join("department", JoinType.LEFT);
                Predicate byDept = cb.like(cb.lower(deptJoin.get("name")), like);

                predicates.add(cb.or(byName, byCode, byDept));
            }

            if (searchDTO.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), searchDTO.getDepartmentId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}













