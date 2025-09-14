package com.example.tasks.authenticationservice.controller;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth/register")
public class RegistrationController {
	private final RegistrationService registrationService;

	public RegistrationController(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	@PostMapping
	public ResponseEntity<RegistrationResponseDto> registerUser(
			@Valid @RequestBody RegistrationRequestDto registrationRequestDto,
			@RequestHeader("X-User-ID") String userId,
			@RequestHeader("X-Internal-Secret") String internalSecret) {

		RegistrationResponseDto responseDto = registrationService.registerUser(registrationRequestDto, userId, internalSecret);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}
}
