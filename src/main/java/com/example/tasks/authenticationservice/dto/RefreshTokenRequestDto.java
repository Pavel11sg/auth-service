package com.example.tasks.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequestDto {
	@NotBlank(message = "RefreshToken should not be empty")
	private String refreshToken;
}
