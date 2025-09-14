package com.example.tasks.authenticationservice.exception;

public class UserServiceClientRequestException extends RuntimeException {
	public UserServiceClientRequestException(String message) {
		super(message);
	}

	public UserServiceClientRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
