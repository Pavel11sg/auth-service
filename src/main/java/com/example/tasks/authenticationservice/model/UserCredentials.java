package com.example.tasks.authenticationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentials {
	@Id
	@Column(name = "id")
	private UUID userId;
	@Column(name = "username", nullable = false, unique = true, length = 50)
	private String username;
	@Column(name = "password", nullable = false, length = 100)
	private String password;
	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;
	@Column(name = "enabled", nullable = false)
	private boolean enabled;
	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked;
	@Column(name = "last_password_change", nullable = false)
	private LocalDateTime lastPasswordChange;
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}
