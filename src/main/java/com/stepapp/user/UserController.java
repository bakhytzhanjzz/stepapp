package com.stepapp.user;

import com.stepapp.config.SecurityUtils;
import com.stepapp.user.dto.UpdateProfileRequest;
import com.stepapp.user.dto.UserMeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me() {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        var user = userService.getByUsernameOrThrow(username);
        return ResponseEntity.ok(UserMeResponse.from(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserMeResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        var user = userService.getByUsernameOrThrow(username);
        var updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(UserMeResponse.from(updated));
    }
}
