package com.example.tasks.authenticationservice.dto.userDto;

import com.example.tasks.authenticationservice.dto.cardDto.CardResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserResponseDto {
	private UUID id;
	private String name;
	private String surname;
	private String maskedEmail;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;
	private List<CardResponseDto> cards;
}
