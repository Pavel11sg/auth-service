package com.example.tasks.authenticationservice.dto.cardDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CardResponseDto {
	private UUID id;
	private String lastFourDigits;
	private String maskedHolder;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate expirationDate;
}
