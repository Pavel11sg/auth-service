package com.example.tasks.authenticationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.collect(Collectors.joining(", "));
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto(
						HttpStatus.BAD_REQUEST.value(),
						"VALIDATION_ERROR",
						message,
						request.getDescription(false).replace("uri=", "")
				));
	}

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDto> handleUsernameExists(UsernameAlreadyExistsException ex, WebRequest request) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(new ErrorResponseDto(
						HttpStatus.CONFLICT.value(),
						"USERNAME_EXISTS",
						ex.getMessage(),
						request.getDescription(false).replace("uri=", "")
				));
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDto> handleEmailExists(EmailAlreadyExistsException ex, WebRequest request) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(new ErrorResponseDto(
						HttpStatus.CONFLICT.value(),
						"EMAIL_EXISTS",
						ex.getMessage(),
						request.getDescription(false).replace("uri=", "")
				));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponseDto> handleBadCredentials(
			BadCredentialsException ex,
			WebRequest request
	) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponseDto(
						HttpStatus.UNAUTHORIZED.value(),
						"AUTH_FAILED",
						ex.getMessage(),
						request.getDescription(false).replace("uri=", "")
				));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponseDto> handleAuthenticationException(
			AuthenticationException ex,
			WebRequest request
	) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponseDto(
						HttpStatus.UNAUTHORIZED.value(),
						"AUTH_FAILED",
						ex.getMessage(),
						request.getDescription(false).replace("uri=", "")
				));
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponseDto(
						HttpStatus.UNAUTHORIZED.value(),
						"INVALID_TOKEN",
						ex.getMessage(),
						request.getDescription(false)
				));
	}

	@ExceptionHandler(PasswordNotStrongException.class)
	public ResponseEntity<ErrorResponseDto> handleBusinessValidation(
			PasswordNotStrongException ex, WebRequest request) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponseDto(
						HttpStatus.BAD_REQUEST.value(),
						"PASSWORD_VALIDATION_ERROR",
						ex.getMessage(),
						request.getDescription(false).replace("uri=", "")
				));
	}
}
