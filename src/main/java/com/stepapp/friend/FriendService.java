package com.stepapp.friend;

import com.stepapp.friend.dto.FriendRequestDto;
import com.stepapp.user.User;
import com.stepapp.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRepo;
    private final UserService userService;

    @Transactional
    public FriendRequestDto sendRequest(String senderUsername, String receiverUsername) {
        if (senderUsername.equals(receiverUsername)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        User sender = userService.getByUsernameOrThrow(senderUsername);
        User receiver = userService.getByUsernameOrThrow(receiverUsername);

        friendRepo.findBySenderAndReceiver(sender, receiver).ifPresent(fr -> {
            throw new IllegalStateException("Friend request already exists");
        });

        FriendRequest fr = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequest.Status.PENDING)
                .build();

        return FriendRequestDto.from(friendRepo.save(fr));
    }

    @Transactional
    public FriendRequestDto respondToRequest(Long requestId, String receiverUsername, boolean accept) {
        FriendRequest fr = friendRepo.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Request not found"));

        if (!fr.getReceiver().getUsername().equals(receiverUsername)) {
            throw new IllegalArgumentException("You are not the receiver of this request");
        }

        fr.setStatus(accept ? FriendRequest.Status.ACCEPTED : FriendRequest.Status.REJECTED);
        return FriendRequestDto.from(friendRepo.save(fr));
    }

    @Transactional(readOnly = true)
    public List<FriendRequestDto> listPendingRequests(String username) {
        User u = userService.getByUsernameOrThrow(username);
        return friendRepo.findAllByReceiverAndStatus(u, FriendRequest.Status.PENDING)
                .stream().map(FriendRequestDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listFriends(String username) {
        User u = userService.getByUsernameOrThrow(username);
        return friendRepo.findAllBySenderOrReceiverAndStatus(u, u, FriendRequest.Status.ACCEPTED)
                .stream()
                .map(fr -> fr.getSender().equals(u) ? fr.getReceiver().getUsername() : fr.getSender().getUsername())
                .toList();
    }
}
