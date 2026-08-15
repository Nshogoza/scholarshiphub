package com.scholarshiphub.repository.specification;

import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.entity.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for the admin user-management listing endpoint. */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> hasRole(RoleName role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role").get("name"), role);
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> emailContains(String search) {
        return (root, query, cb) -> (search == null || search.isBlank())
                ? null
                : cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%");
    }
}
