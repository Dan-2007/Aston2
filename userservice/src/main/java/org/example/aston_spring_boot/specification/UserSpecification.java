package org.example.aston_spring_boot.specification;

import org.example.aston_spring_boot.dto.UserParamsDTO;
import org.example.aston_spring_boot.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserSpecification {
    public Specification<User> build(UserParamsDTO params) {
        return withCreatedAtGt(params.getCreatedAtGt())
                .and(withCreatedAtLt(params.getCreatedAtLt()))
                .and(withAgeGt(params.getAgeGt()))
                .and(withAgeLt(params.getAgeLt()))
                .and(withNameCont(params.getName()));
    }


    private Specification<User> withCreatedAtGt(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() : cb.greaterThan(root.get("createdAt"), date);
    }

    private Specification<User> withCreatedAtLt(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() : cb.lessThan(root.get("createdAt"), date);
    }

    private Specification<User> withAgeGt(Integer ageGt) {
        return (root, query, cb) ->
                ageGt == null ? cb.conjunction() : cb.greaterThan(root.get("age"), ageGt);
    }

    private Specification<User> withAgeLt(Integer ageLt) {
        return (root, query, cb) ->
                ageLt == null ? cb.conjunction() : cb.lessThan(root.get("age"), ageLt);
    }


    private Specification<User> withNameCont(String name){
        return (root, query, cb)
                -> name == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
    }
}