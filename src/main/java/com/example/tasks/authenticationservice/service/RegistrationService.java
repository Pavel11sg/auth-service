package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.exception.EmailAlreadyExistsException;
import com.example.tasks.authenticationservice.exception.UsernameAlreadyExistsException;
import com.example.tasks.authenticationservice.mapper.UserCredentialsMapper;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RegistrationService {
	private final UserCredentialsRepository userCredentialsRepository;
	private final UserCredentialsMapper userCredentialsMapper;
	private final PasswordEncoder passwordEncoder;

	public RegistrationService(UserCredentialsRepository userCredentialsRepository, UserCredentialsMapper userCredentialsMapper, PasswordEncoder passwordEncoder) {
		this.userCredentialsRepository = userCredentialsRepository;
		this.userCredentialsMapper = userCredentialsMapper;
		this.passwordEncoder = passwordEncoder;
	}

	public RegistrationResponseDto registerUser(RegistrationRequestDto registrationRequestDto) {
		if (userCredentialsRepository.existsByUsername(registrationRequestDto.getUsername())) {
			throw new UsernameAlreadyExistsException("User with such username already exists! Please, select different username");
		}
		if (userCredentialsRepository.existsByEmail(registrationRequestDto.getEmail())) {
			throw new EmailAlreadyExistsException("User with such email already exists! Please, select different email");
		}
		if (!isPasswordStrong(registrationRequestDto.getPassword())) {
			throw new IllegalArgumentException("Password must contain 1 digit, 1 uppercase letter, and 1 special character");
		}
		UserCredentials user = userCredentialsMapper.toEntity(registrationRequestDto);
		user.setUserId(UUID.randomUUID());
		user.setPassword(passwordEncoder.encode(registrationRequestDto.getPassword()));
		UserCredentials savedUser = userCredentialsRepository.save(user);
		return userCredentialsMapper.toResponseDto(savedUser);
	}

	private boolean isPasswordStrong(String password) {
		return password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$");
	}
}
