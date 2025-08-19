package com.example.tasks.authenticationservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenResponseDto {
	private String newAccessToken;
	private String newRefreshToken;
	private Long accessTokenExpiresIn;
}
