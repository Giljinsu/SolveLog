package com.study.blog.dto.alarm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class AlarmResponseDto {
    private Long alarmId;
    private String content; // metadata 와 template 조합
    private Boolean isViewed;
    private Long postId;
    private String link; // 이동할 링크
    private Long alarmCnt; // not Viewed 알림 갯수

    public AlarmResponseDto(Long alarmId, String content, Boolean isViewed) {
        this.alarmId = alarmId;
        this.content = content;
        this.isViewed = isViewed;
    }


    public AlarmResponseDto(Long alarmId, String content, Boolean isViewed, Long postId,
        String link, Long alarmCnt) {
        this.alarmId = alarmId;
        this.content = content;
        this.isViewed = isViewed;
        this.postId = postId;
        this.link = link;
        this.alarmCnt = alarmCnt;
    }
}
