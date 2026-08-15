package com.scholarshiphub.repository;

import com.scholarshiphub.entity.User;
import com.scholarshiphub.entity.enums.RoleName;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from User u where u.role.name = :roleName")
    Page<User> findAllByRoleName(RoleName roleName, Pageable pageable);

    long countByRole_Name(RoleName roleName);
}
