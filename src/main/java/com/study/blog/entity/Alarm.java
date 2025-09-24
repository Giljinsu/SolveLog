package com.study.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class Alarm {
    @Id @GeneratedValue
    @Column(name = "alarm_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alarm_type_id")
    private AlarmType alarmType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    private String metadata; // JSON: {"sender": "홍길동", "postTitle": "오늘의 일기"}
    private Boolean isViewed;


    public static Alarm createAlarm(Users users, AlarmType alarmType, String metadata) {
        Alarm newAlarm = new Alarm();
        newAlarm.setUsers(users);
        newAlarm.setAlarmType(alarmType);
        newAlarm.setMetadata(metadata);
        newAlarm.setIsViewed(false);

        return newAlarm;
    }

    public void hasViewed() {
        this.isViewed = true;
    }
}
