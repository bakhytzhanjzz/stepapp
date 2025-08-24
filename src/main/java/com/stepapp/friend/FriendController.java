package com.stepapp.friend;

import com.stepapp.config.SecurityUtils;
import com.stepapp.friend.dto.FriendRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request/{receiverUsername}")
    public ResponseEntity<FriendRequestDto> sendRequest(@PathVariable String receiverUsername) {
        String sender = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(friendService.sendRequest(sender, receiverUsername));
    }

    @PostMapping("/respond/{requestId}")
    public ResponseEntity<FriendRequestDto> respondToRequest(
            @PathVariable Long requestId,
            @RequestParam boolean accept
    ) {
        String receiver = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(friendService.respondToRequest(requestId, receiver, accept));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequestDto>> pendingRequests() {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(friendService.listPendingRequests(username));
    }

    @GetMapping
    public ResponseEntity<List<String>> friends() {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(friendService.listFriends(username));
    }
}
