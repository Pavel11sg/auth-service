package com.example.tasks.authenticationservice.repository;

import com.example.tasks.authenticationservice.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {
	Optional<UserCredentials> findById(UUID userId);
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<UserCredentials> findByUsername(String username);
}
