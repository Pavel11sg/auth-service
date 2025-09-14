package com.example.tasks.authenticationservice.repository;

import com.example.tasks.authenticationservice.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {
	Optional<UserCredentials> findById(UUID userId);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

//	Optional<UserCredentials> findByUsernameWithRoles(String username);

	@Query("SELECT uc FROM UserCredentials uc LEFT JOIN FETCH uc.roles WHERE uc.username = :username")
	Optional<UserCredentials> findByUsernameWithRoles(@Param("username") String username);
}
