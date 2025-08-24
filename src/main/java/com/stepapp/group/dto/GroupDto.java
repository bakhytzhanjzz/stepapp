package com.stepapp.group.dto;

import com.stepapp.group.Group;

public record GroupDto(Long id, String name, String description, boolean isPrivate) {
    public static GroupDto from(Group g) {
        return new GroupDto(g.getId(), g.getName(), g.getDescription(), g.isPrivate());
    }
}
