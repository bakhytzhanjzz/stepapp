package com.stepapp.auth.token;

import com.stepapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Время жизни refresh токена (например, 30 дней)
    private static final long REFRESH_TOKEN_DURATION_MS = 30L * 24 * 60 * 60 * 1000;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Генерация нового refresh токена для пользователя.
     * Можно дополнительно сохранить deviceInfo (User-Agent / clientId).
     */
    public RefreshToken createRefreshToken(User user, String deviceInfo) {
        String token = generateSecureToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Проверка refresh токена. Если просрочен или отозван → Optional.empty().
     */
    public Optional<RefreshToken> verifyToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()));
    }

    /**
     * Отозвать один refresh токен (например при логауте).
     */
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Отозвать все токены пользователя (например при "выйти на всех устройствах").
     */
    public void revokeAll(User user) {
        refreshTokenRepository.findAllByUser(user).forEach(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
