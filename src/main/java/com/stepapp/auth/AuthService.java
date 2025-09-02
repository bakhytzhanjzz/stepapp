package com.stepapp.auth;

import com.stepapp.auth.dto.LoginRequest;
import com.stepapp.auth.dto.LoginResponse;
import com.stepapp.auth.dto.RegisterRequest;
import com.stepapp.auth.dto.RegisterResponse;
import com.stepapp.auth.token.RefreshToken;
import com.stepapp.auth.token.RefreshTokenRepository;
import com.stepapp.auth.token.RefreshTokenService;
import com.stepapp.auth.jwt.JwtService;
import com.stepapp.user.User;
import com.stepapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;


import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already in use");
        }

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .build();

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getUsername(),
                saved.getFullName()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // ищем юзера по email или username
        User user = userRepository.findByEmail(request.usernameOrEmail())
                .or(() -> userRepository.findByUsername(request.usernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getUsername());

        // для простоты deviceInfo возьмем null или позже будем передавать из контроллера
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, null);

        return new LoginResponse(
                accessToken,
                LoginResponse.BEARER,
                refreshToken.getToken()
        );
    }

    public Optional<LoginResponse> refreshToken(String refreshTokenRaw) {
        return refreshTokenService.verifyToken(refreshTokenRaw)
                .map(rt -> {
                    var user = rt.getUser();
                    // генерируем новый access token
                    String newAccessToken = jwtService.generateToken(user.getUsername());

                    // создаём новый refresh token (ротация)
                    var newRt = refreshTokenService.createRefreshToken(user, rt.getDeviceInfo());

                    // старый помечаем как отозванный
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);

                    return new LoginResponse(
                            newAccessToken,
                            LoginResponse.BEARER,
                            newRt.getToken()
                    );
                });
    }

    @Value("${google.client-id}")
    private String googleClientId;

    public LoginResponse loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");

            // ищем юзера по email
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                // если нет — создаём
                User newUser = User.builder()
                        .email(email)
                        .username(email.split("@")[0]) // простой username
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString())) // случайный пароль
                        .fullName(fullName)
                        .avatarUrl(avatarUrl)
                        .build();
                return userRepository.save(newUser);
            });

            // выдаём токены
            String accessToken = jwtService.generateToken(user.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, "google-oauth");

            return new LoginResponse(
                    accessToken,
                    LoginResponse.BEARER,
                    refreshToken.getToken()
            );

        } catch (Exception e) {
            throw new RuntimeException("Google login failed", e);
        }
    }
}
