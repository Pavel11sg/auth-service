package com.example.tasks.authenticationservice.controller;

import com.example.tasks.authenticationservice.service.LogoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/logout")
public class LogoutController {
	private final LogoutService logoutService;

	public LogoutController(LogoutService logoutService) {
		this.logoutService = logoutService;
	}

	@PostMapping
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
		logoutService.logout(authHeader);
		return ResponseEntity.noContent().build();
	}
}
