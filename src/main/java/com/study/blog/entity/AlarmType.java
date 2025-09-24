package com.study.blog.entity;

import com.study.blog.entity.enums.AlarmTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AlarmType {
    @Id @GeneratedValue
    @Column(name = "alarm_type_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private AlarmTypeEnum type;
    private String template; // ex "{{sender}}님이 '{{postTitle}}'에 댓글을 남겼습니다."

    private AlarmType(AlarmTypeEnum type, String template) {
        this.type = type;
        this.template = template;
    }

    public static AlarmType createAlarmType(AlarmTypeEnum type, String template) {
        return new AlarmType(type, template);
    }
}
