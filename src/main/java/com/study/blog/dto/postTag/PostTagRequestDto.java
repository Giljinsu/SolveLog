package com.study.blog.dto.postTag;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PostTagRequestDto {
    private String username;
    private String categoryType;
}
