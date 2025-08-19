package com.example.tasks.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
	@NotBlank(message = "Username cannot be empty")
	private String username;
	@NotBlank(message = "Password cannot be empty")
	private String password;
}
