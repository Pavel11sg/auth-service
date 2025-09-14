package com.example.tasks.authenticationservice.mapper;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.model.UserCredentials;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class UserCredentialsMapper {
	public UserCredentials toEntity(RegistrationRequestDto dto) {
		return UserCredentials.builder()
				.username(dto.getUsername())
				.email(dto.getEmail())
				.password(dto.getPassword())
				.enabled(true)
				.accountNonLocked(true)
				.createdAt(LocalDateTime.now())
				.lastPasswordChange(LocalDateTime.now())
				.build();
	}

	public RegistrationResponseDto toResponseDto(UserCredentials entity) {
		return RegistrationResponseDto.builder()
				.userId(entity.getUserId())
				.username(entity.getUsername())
				.email(entity.getEmail())
				.roles(entity.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
				.createdAt(entity.getCreatedAt())
				.build();
	}
}
