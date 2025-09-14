package com.example.tasks.authenticationservice.repository;

import com.example.tasks.authenticationservice.model.Role;
import com.example.tasks.authenticationservice.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
	Optional<Role> findByName(RoleName name);
}
