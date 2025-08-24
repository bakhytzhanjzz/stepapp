package com.stepapp.friend;

import com.stepapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FriendRequest> findAllByReceiverAndStatus(User receiver, FriendRequest.Status status);

    List<FriendRequest> findAllBySenderAndStatus(User sender, FriendRequest.Status status);

    List<FriendRequest> findAllByStatusAndSenderOrReceiver(FriendRequest.Status status, User sender, User receiver);

    List<FriendRequest> findAllBySenderOrReceiverAndStatus(User sender, User receiver, FriendRequest.Status status);
}
