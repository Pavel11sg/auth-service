package com.example.tasks.authenticationservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ValidateTokenResponseDto {
	boolean isValid;
	private UUID userId;
	private String message;
}
