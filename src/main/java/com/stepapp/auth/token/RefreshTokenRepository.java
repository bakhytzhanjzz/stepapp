package com.stepapp.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

import com.stepapp.user.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Вернуть все токены пользователя (например, для отображения устройств или ревокации).
     */
    List<RefreshToken> findAllByUser(User user);

    /**
     * Удалить все токены пользователя (например, "выйти на всех устройствах").
     */
    int deleteAllByUser(User user);

    /**
     * Удалить по конкретному токену.
     */
    void deleteByToken(String token);

    /**
     * Быстрая проверка на существование неотозванного токена.
     */
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
}
