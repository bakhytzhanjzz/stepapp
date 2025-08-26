package com.stepapp.group;

import com.stepapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    List<GroupInvitation> findAllByInvited(User invited);
    Optional<GroupInvitation> findByGroupAndInvited(Group group, User invited);

    // --- Добавляем этот метод для получения всех PENDING приглашений ---
    List<GroupInvitation> findAllByInvitedAndStatus(User invited, GroupInvitation.Status status);
}
