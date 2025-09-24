package com.study.blog.controller;

import com.study.blog.dto.alarm.AlarmRequestDto;
import com.study.blog.dto.alarm.AlarmResponseDto;
import com.study.blog.service.AlarmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/api/getAlarmList/{username}")
    public ResponseEntity<Result<List<AlarmResponseDto>>> getAlarmLst(
        @PathVariable String username) {
        return ResponseEntity.ok(Result.of(alarmService.getAlarmsByUser(username)));
    }

    @PostMapping("/api/createAlarm")
    public ResponseEntity<Void> createAlarm(@ModelAttribute AlarmRequestDto alarmRequestDto) {
        alarmService.createAlarm(alarmRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/viewAlarm/{alarmId}")
    public ResponseEntity<Void> viewAlarm(@PathVariable Long alarmId) {
        alarmService.viewAlarm(alarmId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/deleteAlarm/{alarmId}")
    public ResponseEntity<Void> deleteAlarm(@PathVariable Long alarmId) {
        alarmService.deleteAlarm(alarmId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/deleteAlarmsByUsername/{username}")
    public ResponseEntity<Void> deleteAlarmsByUsername(@PathVariable String username) {
        alarmService.deleteAllAlarmByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/updateAlarmsIsTrue/{username}")
    public ResponseEntity<Void> updateAlarmIsViewedTrueByUsername(@PathVariable String username) {
        alarmService.updateAlarmIsViewedTrueByUsername(username);
        return ResponseEntity.noContent().build();
    }
}
