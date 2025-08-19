package com.example.tasks.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDto {
	@NotBlank(message = "Username can not be empty")
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	private String username;
	@NotBlank(message = "Email cannot be blank")
	@Size(max = 100, message = "length should be withing 100 symbols")
	@Email(message = "Email should be valid")
	private String email;
	@NotBlank(message = "Password cannot be blank")
	@Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
	private String password;
}
