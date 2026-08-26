package com.study.blog.admin.dto;

import com.study.blog.entity.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class AdminRoleUpdateRequestDto {

    @NotNull
    private Role role;
}
