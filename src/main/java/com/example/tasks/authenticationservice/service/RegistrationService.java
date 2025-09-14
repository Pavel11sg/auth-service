package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.exception.EmailAlreadyExistsException;
import com.example.tasks.authenticationservice.exception.PasswordNotStrongException;
import com.example.tasks.authenticationservice.exception.RoleNotFoundException;
import com.example.tasks.authenticationservice.exception.UsernameAlreadyExistsException;
import com.example.tasks.authenticationservice.mapper.UserCredentialsMapper;
import com.example.tasks.authenticationservice.model.Role;
import com.example.tasks.authenticationservice.model.RoleName;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.repository.RoleRepository;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class RegistrationService {
	private final UserCredentialsRepository userCredentialsRepository;
	private final RoleRepository roleRepository;
	private final UserCredentialsMapper userCredentialsMapper;
	private final PasswordEncoder passwordEncoder;
	@Value("${gateway.internal.secret}")
	private String internalSecret;
	//private final UserServiceClient userServiceClient;

	public RegistrationService(UserCredentialsRepository userCredentialsRepository, RoleRepository roleRepository, UserCredentialsMapper userCredentialsMapper, PasswordEncoder passwordEncoder) {
		this.userCredentialsRepository = userCredentialsRepository;
		this.roleRepository = roleRepository;
		this.userCredentialsMapper = userCredentialsMapper;
		this.passwordEncoder = passwordEncoder;
		//this.userServiceClient = userServiceClient;
	}

	public RegistrationResponseDto registerUser(RegistrationRequestDto registrationRequestDto, String id, String passedSecret) {
		if (!internalSecret.equals(passedSecret)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
		}
		if (userCredentialsRepository.existsByUsername(registrationRequestDto.getUsername())) {
			throw new UsernameAlreadyExistsException("User with such username already exists! Please, select different username");
		}
		if (userCredentialsRepository.existsByEmail(registrationRequestDto.getEmail())) {
			throw new EmailAlreadyExistsException("User with such email already exists! Please, select different email");
		}
		if (!isPasswordStrong(registrationRequestDto.getPassword())) {
			throw new PasswordNotStrongException("Password must contain 1 digit, 1 uppercase letter, and 1 special character");
		}
		UUID userId = UUID.fromString(id);
		Role userRole = roleRepository.findByName(RoleName.USER)
				.orElseThrow(() -> new RoleNotFoundException("Role USER not found"));

		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		UserCredentials user = userCredentialsMapper.toEntity(registrationRequestDto);
		user.setUserId(userId);
		user.setPassword(passwordEncoder.encode(registrationRequestDto.getPassword()));
		user.setRoles(roles);
		UserCredentials savedUser = userCredentialsRepository.save(user);
		return userCredentialsMapper.toResponseDto(savedUser);
	}
	//	public RegistrationResponseDto registerUser(RegistrationRequestDto registrationRequestDto) {
	//		if (userCredentialsRepository.existsByUsername(registrationRequestDto.getUsername())) {
	//			throw new UsernameAlreadyExistsException("User with such username already exists! Please, select different username");
	//		}
	//		if (userCredentialsRepository.existsByEmail(registrationRequestDto.getEmail())) {
	//			throw new EmailAlreadyExistsException("User with such email already exists! Please, select different email");
	//		}
	//		if (!isPasswordStrong(registrationRequestDto.getPassword())) {
	//			throw new PasswordNotStrongException("Password must contain 1 digit, 1 uppercase letter, and 1 special character");
	//		}
	//		UUID userId = null;
	//		try {
	//			UserRequestDto userRequestDto = convertToUserRequestDto(registrationRequestDto);
	//
	//			ResponseEntity<UserResponseDto> userResponse = userServiceClient.createUser(userRequestDto);
	//			if (!userResponse.getStatusCode().is2xxSuccessful()) {
	//				throw new RuntimeException("User service returned non-success status: " + userResponse.getStatusCode());
	//			}
	//			userId = userResponse.getBody().getId();
	//			log.info("Successfully created user in user service with ID: {}", userId);
	//
	//			Role userRole = roleRepository.findByName(RoleName.USER).orElseThrow(() -> new RoleNotFoundException("Role USER not found"));
	//			Set<Role> roles = new HashSet<>();
	//			roles.add(userRole);
	//
	//			UserCredentials user = userCredentialsMapper.toEntity(registrationRequestDto);
	//			user.setUserId(userId);
	//			user.setPassword(passwordEncoder.encode(registrationRequestDto.getPassword()));
	//			user.setRoles(roles);
	//
	//			UserCredentials savedUser = userCredentialsRepository.save(user);
	//			log.info("Successfully created auth credentials for user ID: {}", userId);
	//			return userCredentialsMapper.toResponseDto(savedUser);
	//		} catch (FeignException e) {
	//			log.error("Feign client error when calling user service: {}", e.getMessage());
	//			throw new RuntimeException("Failed to communicate with user service: " + e.getMessage(), e);
	//		} catch (Exception e) {
	//			if (userId != null) {
	//				try {
	//					userServiceClient.deleteUser(userId);
	//					log.info("Rollback: Deleted user from user service with ID: {}", userId);
	//				} catch (Exception rollbackException) {
	//					log.error("Failed to rollback user creation for ID: {}", userId, rollbackException);
	//				}
	//			}
	//			throw new RuntimeException("Registration failed: " + e.getMessage(), e);
	//		}
	//	}

	private boolean isPasswordStrong(String password) {
		return password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$");
	}
	//	private UserRequestDto convertToUserRequestDto(RegistrationRequestDto registrationRequestDto) {
	//		return UserRequestDto.builder()
	//				.name(registrationRequestDto.getName())
	//				.surname(registrationRequestDto.getSurname())
	//				.email(registrationRequestDto.getEmail())
	//				.birthDate(registrationRequestDto.getBirthDate())
	//				.build();
	//	}
}
