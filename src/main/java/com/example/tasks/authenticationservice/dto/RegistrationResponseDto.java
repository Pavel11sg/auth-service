package com.example.tasks.authenticationservice.dto;

import com.example.tasks.authenticationservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDto {
	private UUID userId;
	private String username;
	private String email;
	private LocalDateTime createdAt;
	private Set<String> roles;
}
