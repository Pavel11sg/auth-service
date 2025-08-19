package com.example.tasks.authenticationservice.exception;

public class InvalidTokenException extends RuntimeException {
	public InvalidTokenException(String msg) {
		super(msg);
	}
}
