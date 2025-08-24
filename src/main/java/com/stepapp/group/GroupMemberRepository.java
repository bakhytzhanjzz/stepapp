package com.stepapp.group;

import com.stepapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findAllByUser(User user);
    List<GroupMember> findAllByGroup(Group group);
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
}
