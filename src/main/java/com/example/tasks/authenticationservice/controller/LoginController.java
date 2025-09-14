package com.example.tasks.authenticationservice.controller;

import com.example.tasks.authenticationservice.dto.LoginRequestDto;
import com.example.tasks.authenticationservice.dto.LoginResponseDto;
import com.example.tasks.authenticationservice.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/login")
public class LoginController {
	private final LoginService authenticationService;

	public LoginController(LoginService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping
	public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
		return ResponseEntity.ok(authenticationService.login(loginRequestDto));
	}
}
