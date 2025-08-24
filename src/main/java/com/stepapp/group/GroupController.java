package com.stepapp.group;

import com.stepapp.config.SecurityUtils;
import com.stepapp.group.dto.GroupDto;
import com.stepapp.group.dto.GroupInvitationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@RequestParam String name,
                                                @RequestParam(required = false) String description,
                                                @RequestParam(defaultValue = "false") boolean isPrivate) {
        String owner = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(groupService.createGroup(owner, name, description, isPrivate));
    }

    @PostMapping("/{groupId}/invite/{username}")
    public ResponseEntity<GroupInvitationDto> invite(@PathVariable Long groupId,
                                                     @PathVariable String username) {
        String inviter = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(groupService.inviteUser(groupId, inviter, username));
    }

    @PostMapping("/invitations/{inviteId}")
    public ResponseEntity<GroupInvitationDto> respond(@PathVariable Long inviteId,
                                                      @RequestParam boolean accept) {
        String invited = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(groupService.respondToInvite(inviteId, invited, accept));
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> myGroups() {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(groupService.listUserGroups(username));
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        groupService.leaveGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        groupService.deleteGroup(groupId, username);
        return ResponseEntity.noContent().build();
    }
}
