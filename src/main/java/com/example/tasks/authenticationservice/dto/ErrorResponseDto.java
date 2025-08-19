package com.example.tasks.authenticationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorResponseDto {
	private Instant timestamp;
	private int status;
	private String error;
	private String message;
}
