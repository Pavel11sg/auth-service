package com.example.tasks.authenticationservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class LoginResponseDto {
	private String accessToken;
	private String refreshToken;
	private String type;
	private UUID userId;
	private String username;
	private String email;
	private Long accessTokenExpiresIn;
	private Set<String> roles;
}
