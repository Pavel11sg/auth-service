package com.example.tasks.authenticationservice.security;

import com.example.tasks.authenticationservice.exception.InvalidTokenException;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.service.TokenBlacklistAndStorageService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {
	private final TokenBlacklistAndStorageService blacklistAndStorageService;

	private final SecretKey signingKey;

	public JwtUtils(TokenBlacklistAndStorageService blacklistService, @Value("${jwt.secret}") String secret) {
		this.blacklistAndStorageService = blacklistService;
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Value("${jwt.access.expirationMs}")
	private long accessTokenExpirationMs;

	@Value("${jwt.refresh.expirationMs}")
	private long refreshTokenExpirationMs;

	public String generateAccessToken(UserDetailsImpl userDetails) {
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(userDetails.getUsername())
				.claim("userId", userDetails.getUserCredentials().getUserId().toString())
				.issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plusMillis(accessTokenExpirationMs)))
				.signWith(getSigningKey(), Jwts.SIG.HS256)
				.compact();
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(userDetails.getUsername())
				.issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plusMillis(refreshTokenExpirationMs)))
				.signWith(getSigningKey(), Jwts.SIG.HS256)
				.compact();
	}

	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.getSubject();
	}

	public boolean validateAccessToken(String token) {
		if (token == null || token.isBlank()) {
			throw new InvalidTokenException("Token cannot be empty");
		}
		if (isTokenExpired(token)) {
			throw new InvalidTokenException("Token expired");
		}
		if (blacklistAndStorageService.isAccessTokenRevoked(token)) {
			throw new InvalidTokenException("Token revoked");
		}
		try {
			Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.error("ExpiredJwtException caught, throwing InvalidTokenException");
			throw new InvalidTokenException("Token expired");
		} catch (JwtException | IllegalArgumentException e) {
			log.error("JwtException caught, throwing InvalidTokenException");
			throw new InvalidTokenException("Invalid token");
		}
	}

	public boolean validateRefreshToken(String token) {
		if (token == null || token.isBlank()) {
			throw new InvalidTokenException("Token cannot be empty");
		}
		try {
			Claims claims = Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
			if (claims.getExpiration().toInstant().isBefore(Instant.now())) {
				return false;
			}
			return blacklistAndStorageService.isRefreshTokenStored(token);
		} catch (ExpiredJwtException e) {
			log.error("ExpiredJwtException caught, throwing InvalidTokenException");
			throw new InvalidTokenException("Token expired");
		} catch (JwtException | IllegalArgumentException e) {
			log.error("JwtException caught, throwing InvalidTokenException");
			throw new InvalidTokenException("Invalid token");
		}
	}

	public void revokeAccessToken(String token) {
		blacklistAndStorageService.addAccessTokenToBlacklist(token);
	}

	public void saveRefreshToken(String token) {
		blacklistAndStorageService.addRefreshTokenToStorage(token);
	}

	public void removeRefreshToken(String token) {
		blacklistAndStorageService.removeRefreshTokenFromStorage(token);
	}

	public String parseJwt(String authHeader) {
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			throw new InvalidTokenException("Missing or invalid Authorization header");
		}
		return authHeader.substring(7);
	}

	public long getAccessTokenExpirationMs() {
		return accessTokenExpirationMs;
	}

	private SecretKey getSigningKey() {
		return signingKey;
	}

	private boolean isTokenExpired(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return claims.getExpiration().toInstant().isBefore(Instant.now());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (JwtException e) {
			log.error("JWT validation failed", e);
			throw new InvalidTokenException("Invalid token");
		}
	}

	public UUID getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return UUID.fromString(claims.get("userId", String.class));
	}
}