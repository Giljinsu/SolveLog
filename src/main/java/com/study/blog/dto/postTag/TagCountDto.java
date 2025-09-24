package com.study.blog.dto.postTag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagCountDto {
    private Long tagId;
    private String tagName;
    private Long count;
}
