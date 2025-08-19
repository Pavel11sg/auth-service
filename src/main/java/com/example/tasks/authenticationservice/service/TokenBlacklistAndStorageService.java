package com.example.tasks.authenticationservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
public class TokenBlacklistAndStorageService {

	private final RedisTemplate<String, Object> redisTemplate;
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.blacklist.prefix}")
	private String blacklistPrefix;
	@Value("${jwt.refresh.prefix}")
	private String refreshPrefix;

	public TokenBlacklistAndStorageService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void addAccessTokenToBlacklist(String token) {
		try {
			String jti = extractJtiFromToken(token);
			long ttlMlSeconds = getNewTtl(token, 10);
			String blackListKey = blacklistPrefix + jti;
			redisTemplate.opsForValue().set(blackListKey, "revoked");
			redisTemplate.expire(blackListKey, Duration.ofMillis(ttlMlSeconds));
			log.info("Access token with JTI {} added to blacklist. TTL: {} seconds", jti, ttlMlSeconds);
		} catch (Exception e) {
			log.error("Failed to blacklist token: {}", e.getMessage());
			throw new IllegalStateException("Token blacklisting failed", e);
		}
	}

	public void addRefreshTokenToStorage(String token) {
		try {
			String jti = extractJtiFromToken(token);
			long ttlMlSeconds = getNewTtl(token, 60);
			String storageKey = refreshPrefix + jti;
			redisTemplate.opsForValue().set(storageKey, true);
			redisTemplate.expire(storageKey, Duration.ofMillis(ttlMlSeconds));
			log.info("Refresh token with JTI {} stored. TTL: {} ms", jti, ttlMlSeconds);
		} catch (Exception e) {
			log.error("Failed to store refresh token: {}", e.getMessage());
			throw new IllegalStateException("Refresh token storage failed", e);
		}
	}

	public void removeRefreshTokenFromStorage(String token) {
		String jti = extractJtiFromToken(token);
		String storageKey = refreshPrefix + jti;
		redisTemplate.delete(storageKey);
	}

	public boolean isAccessTokenRevoked(String token) {
		try {
			String jti = extractJtiFromToken(token);
			String key = blacklistPrefix + jti;
			return Boolean.TRUE.equals(redisTemplate.hasKey(key));
		} catch (Exception e) {
			log.error("Redis check failed", e);
			return false;
		}
	}

	public boolean isRefreshTokenStored(String token) {
		try {
			String jti = extractJtiFromToken(token);
			String storageKey = refreshPrefix + jti;
			return Boolean.TRUE.equals(redisTemplate.hasKey(storageKey));
		} catch (Exception e) {
			log.error("Redis check failed", e);
			return false;
		}
	}

	private long getNewTtl(String token, long additionalSec) {
		SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		JwtParser parser = Jwts.parser()
				.verifyWith(key)
				.build();
		Jws<Claims> jws = parser.parseSignedClaims(token);
		Claims claims = jws.getPayload();
		long expirationMs = claims.getExpiration().getTime();
		long mlSecondsRemaining = (expirationMs - System.currentTimeMillis());
		return mlSecondsRemaining + additionalSec * 1000;
	}

	public String extractJtiFromToken(String token) {
		SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.getId();
	}
}
