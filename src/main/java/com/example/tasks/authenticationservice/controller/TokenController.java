package com.example.tasks.authenticationservice.controller;

import com.example.tasks.authenticationservice.dto.RefreshTokenRequestDto;
import com.example.tasks.authenticationservice.dto.RefreshTokenResponseDto;
import com.example.tasks.authenticationservice.dto.ValidateTokenResponseDto;
import com.example.tasks.authenticationservice.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class TokenController {
	private final TokenService tokenService;

	public TokenController(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@PostMapping("/validate")
	public ResponseEntity<ValidateTokenResponseDto> validateToken(@Valid @RequestHeader("Authorization") String authHeader) {
		return ResponseEntity.ok(tokenService.validateToken(authHeader));
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
		return ResponseEntity.ok(tokenService.refreshToken(refreshTokenRequestDto));
	}
}
