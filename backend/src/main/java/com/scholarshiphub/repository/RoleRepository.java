package com.scholarshiphub.repository;

import com.scholarshiphub.entity.Role;
import com.scholarshiphub.entity.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
