package com.stepapp.auth.token;

import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;
import com.stepapp.user.User;

/**
 * Refresh token entity.
 * Храним уникальный токен, время истечения, привязку к пользователю и простую мета-информацию о устройстве.
 *
 * Замечания по безопасности:
 * - В идеале хранить не «сырой» refresh token, а его хэш (SHA-256) в БД.
 *   Тогда при верификации нужно будет хешировать приходящий токен и сравнивать.
 * - Поле deviceInfo позволяет привязать токен к конкретному устройству/user-agent.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_token", columnList = "token"),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many refresh tokens may belong to one user (multi-device support).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Храним токен (в продакшене рекомендуется хранить хэш).
     */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /**
     * Время истечения действия токена.
     */
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    /**
     * Время создания (автоматически заполняется).
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Пометка об отзыве токена (например, при логауте или детекте злоупотребления).
     */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Доп. информация об устройстве (user-agent, device id и т.п.)
     */
    @Column(name = "device_info")
    private String deviceInfo;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
