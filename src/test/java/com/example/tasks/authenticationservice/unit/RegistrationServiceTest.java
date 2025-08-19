package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.exception.EmailAlreadyExistsException;
import com.example.tasks.authenticationservice.exception.UsernameAlreadyExistsException;
import com.example.tasks.authenticationservice.mapper.UserCredentialsMapper;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import com.example.tasks.authenticationservice.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
	@Mock
	private UserCredentialsRepository userCredentialsRepository;

	@Mock
	private UserCredentialsMapper userCredentialsMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private RegistrationService registrationService;

	@Test
	void registerUser_WithValidData_ShouldReturnRegistrationResponse() {
		// Arrange
		RegistrationRequestDto request = TestDataHelper.createValidRegistrationRequest();
		UserCredentials user = TestDataHelper.createUserCredentials();
		UserCredentials savedUser = TestDataHelper.createSavedUserCredentials();
		RegistrationResponseDto expectedResponse = TestDataHelper.createRegistrationResponse();
		when(userCredentialsRepository.existsByUsername(request.getUsername())).thenReturn(false);
		when(userCredentialsRepository.existsByEmail(request.getEmail())).thenReturn(false);
		when(userCredentialsMapper.toEntity(request)).thenReturn(user);
		when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
		when(userCredentialsRepository.save(any(UserCredentials.class))).thenReturn(savedUser);
		when(userCredentialsMapper.toResponseDto(savedUser)).thenReturn(expectedResponse);
		// Act
		RegistrationResponseDto response = registrationService.registerUser(request);
		// Assert
		assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
		verify(userCredentialsRepository).existsByUsername(request.getUsername());
		verify(userCredentialsRepository).existsByEmail(request.getEmail());
		verify(passwordEncoder).encode(request.getPassword());
		verify(userCredentialsRepository).save(user);
	}

	@Test
	void registerUser_WithExistingUsername_ShouldThrowException() {
		// Arrange
		RegistrationRequestDto request = TestDataHelper.createValidRegistrationRequest();
		when(userCredentialsRepository.existsByUsername(request.getUsername())).thenReturn(true);
		// Act & Assert
		assertThatThrownBy(() -> registrationService.registerUser(request))
				.isInstanceOf(UsernameAlreadyExistsException.class)
				.hasMessage("User with such username already exists! Please, select different username");
		verify(userCredentialsRepository).existsByUsername(request.getUsername());
		verifyNoMoreInteractions(userCredentialsRepository);
		verifyNoInteractions(userCredentialsMapper, passwordEncoder);
	}

	@Test
	void registerUser_WithExistingEmail_ShouldThrowException() {
		// Arrange
		RegistrationRequestDto request = TestDataHelper.createValidRegistrationRequest();
		when(userCredentialsRepository.existsByUsername(request.getUsername())).thenReturn(false);
		when(userCredentialsRepository.existsByEmail(request.getEmail())).thenReturn(true);
		// Act & Assert
		assertThatThrownBy(() -> registrationService.registerUser(request))
				.isInstanceOf(EmailAlreadyExistsException.class)
				.hasMessage("User with such email already exists! Please, select different email");
		verify(userCredentialsRepository).existsByUsername(request.getUsername());
		verify(userCredentialsRepository).existsByEmail(request.getEmail());
		verifyNoMoreInteractions(userCredentialsRepository);
		verifyNoInteractions(userCredentialsMapper, passwordEncoder);
	}

	@Test
	void registerUser_WithWeakPassword_ShouldThrowException() {
		// Arrange
		RegistrationRequestDto request = TestDataHelper.createRegistrationRequestWithWeakPassword();
		when(userCredentialsRepository.existsByUsername(request.getUsername())).thenReturn(false);
		when(userCredentialsRepository.existsByEmail(request.getEmail())).thenReturn(false);
		// Act & Assert
		assertThatThrownBy(() -> registrationService.registerUser(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Password must contain 1 digit, 1 uppercase letter, and 1 special character");
		verify(userCredentialsRepository).existsByUsername(request.getUsername());
		verify(userCredentialsRepository).existsByEmail(request.getEmail());
		verifyNoMoreInteractions(userCredentialsRepository);
		verifyNoInteractions(userCredentialsMapper, passwordEncoder);
	}

	@Test
	void registerUser_ShouldEncodePassword() {
		// Arrange
		RegistrationRequestDto request = TestDataHelper.createValidRegistrationRequest();
		UserCredentials user = TestDataHelper.createUserCredentials();
		UserCredentials savedUser = TestDataHelper.createSavedUserCredentials();
		when(userCredentialsRepository.existsByUsername(request.getUsername())).thenReturn(false);
		when(userCredentialsRepository.existsByEmail(request.getEmail())).thenReturn(false);
		when(userCredentialsMapper.toEntity(request)).thenReturn(user);
		when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
		when(userCredentialsRepository.save(any(UserCredentials.class))).thenReturn(savedUser);
		when(userCredentialsMapper.toResponseDto(savedUser)).thenReturn(new RegistrationResponseDto());
		// Act
		registrationService.registerUser(request);
		// Assert
		verify(passwordEncoder).encode(request.getPassword());
		assertThat(user.getPassword()).isEqualTo("encodedPassword");
	}
}