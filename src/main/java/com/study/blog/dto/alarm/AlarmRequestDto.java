package com.study.blog.dto.alarm;

import com.study.blog.entity.enums.AlarmTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class AlarmRequestDto {
    private Long alarmId;
    private Long userId;
    private Long alarmTypeId;
    private String username;
    private AlarmTypeEnum alarmType;

    private String metadata;
    private Boolean isViewed;

    public AlarmRequestDto(String metadata, AlarmTypeEnum alarmType, String username) {
        this.metadata = metadata;
        this.alarmType = alarmType;
        this.username = username;
    }

    public AlarmRequestDto(Boolean isViewed, String metadata, AlarmTypeEnum alarmType, String username) {
        this.isViewed = isViewed;
        this.metadata = metadata;
        this.alarmType = alarmType;
        this.username = username;
    }
}
