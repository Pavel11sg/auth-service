package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.service.TokenBlacklistAndStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistAndStorageService Tests")
class TokenBlacklistAndStorageServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private TokenBlacklistAndStorageService tokenService;

	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		ReflectionTestUtils.setField(tokenService, "secret", TestDataHelper.SECRET);
		ReflectionTestUtils.setField(tokenService, "blacklistPrefix", TestDataHelper.BLACKLIST_PREFIX);
		ReflectionTestUtils.setField(tokenService, "refreshPrefix", TestDataHelper.REFRESH_PREFIX);
	}

	@Nested
	@DisplayName("Access Token Blacklist Operations")
	class AccessTokenTests {

		@Test
		@DisplayName("Should store access token in blacklist with correct TTL")
		void addAccessTokenToBlacklist_ShouldStoreTokenWithCorrectTTL() {
			// Arrange
			doNothing().when(valueOperations).set(anyString(), any());
			// Act
			tokenService.addAccessTokenToBlacklist(TestDataHelper.VALID_TOKEN);
			// Assert
			verify(valueOperations).set(
					eq(TestDataHelper.getBlacklistKey()),
					eq(TestDataHelper.BLACKLIST_TOKEN_VALUE)
			);
			verify(redisTemplate).expire(
					eq(TestDataHelper.getBlacklistKey()),
					any(Duration.class)
			);
		}

		@Test
		@DisplayName("Should throw exception when Redis fails during blacklisting")
		void addAccessTokenToBlacklist_ShouldThrowExceptionWhenRedisFails() {
			// Arrange
			doThrow(new RuntimeException("Redis error"))
					.when(valueOperations)
					.set(anyString(), any());
			// Act & Assert
			assertThatThrownBy(() -> tokenService.addAccessTokenToBlacklist(TestDataHelper.VALID_TOKEN))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("Token blacklisting failed")
					.hasCauseInstanceOf(RuntimeException.class);
			verify(redisTemplate, never()).expire(anyString(), any());
		}

		@Test
		@DisplayName("Should return true when access token is in blacklist")
		void isAccessTokenRevoked_ShouldReturnTrueWhenTokenInBlacklist() {
			// Arrange
			when(redisTemplate.hasKey(TestDataHelper.getBlacklistKey())).thenReturn(true);
			// Act & Assert
			assertThat(tokenService.isAccessTokenRevoked(TestDataHelper.VALID_TOKEN)).isTrue();
		}

		@Test
		@DisplayName("Should return false when access token is not in blacklist")
		void isAccessTokenRevoked_ShouldReturnFalseWhenTokenNotInBlacklist() {
			// Arrange
			when(redisTemplate.hasKey(anyString())).thenReturn(false);
			// Act & Assert
			assertThat(tokenService.isAccessTokenRevoked(TestDataHelper.VALID_TOKEN)).isFalse();
		}

		@Test
		@DisplayName("Should return false when Redis check fails")
		void isAccessTokenRevoked_ShouldReturnFalseWhenExceptionOccurs() {
			// Arrange
			when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis error"));
			// Act & Assert
			assertThat(tokenService.isAccessTokenRevoked(TestDataHelper.VALID_TOKEN)).isFalse();
		}
	}

	@Nested
	@DisplayName("Refresh Token Storage Operations")
	class RefreshTokenTests {

		@Test
		@DisplayName("Should store refresh token with correct TTL")
		void addRefreshTokenToStorage_ShouldStoreTokenWithCorrectTTL() {
			// Arrange
			doNothing().when(valueOperations).set(anyString(), any());
			// Act
			tokenService.addRefreshTokenToStorage(TestDataHelper.VALID_TOKEN);
			// Assert
			verify(valueOperations).set(
					eq(TestDataHelper.getRefreshKey()),
					eq(TestDataHelper.STORAGE_TOKEN_VALUE)
			);
			verify(redisTemplate).expire(
					eq(TestDataHelper.getRefreshKey()),
					any(Duration.class)
			);
		}

		@Test
		@DisplayName("Should delete refresh token from storage")
		void removeRefreshTokenFromStorage_ShouldDeleteKey() {
			// Act
			tokenService.removeRefreshTokenFromStorage(TestDataHelper.VALID_TOKEN);
			// Assert
			verify(redisTemplate).delete(TestDataHelper.getRefreshKey());
		}

		@Test
		@DisplayName("Should return true when refresh token is in storage")
		void isRefreshTokenStored_ShouldReturnTrueWhenTokenInStorage() {
			// Arrange
			when(redisTemplate.hasKey(TestDataHelper.getRefreshKey())).thenReturn(true);
			// Act & Assert
			assertThat(tokenService.isRefreshTokenStored(TestDataHelper.VALID_TOKEN)).isTrue();
		}

		@Test
		@DisplayName("Should return false when refresh token is not in storage")
		void isRefreshTokenStored_ShouldReturnFalseWhenTokenNotInStorage() {
			// Arrange
			when(redisTemplate.hasKey(TestDataHelper.getRefreshKey())).thenReturn(false);
			// Act & Assert
			assertThat(tokenService.isRefreshTokenStored(TestDataHelper.VALID_TOKEN)).isFalse();
		}
	}
}