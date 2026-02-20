package com.study.blog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.dto.alarm.AlarmRequestDto;
import com.study.blog.dto.alarm.AlarmResponseDto;
import com.study.blog.entity.Alarm;
import com.study.blog.entity.AlarmType;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.AlarmTypeEnum;
import com.study.blog.repository.AlarmRepository;
import com.study.blog.repository.AlarmTypeRepository;
import com.study.blog.repository.UsersRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final UsersRepository usersRepository;
    private final AlarmTypeRepository alarmTypeRepository;
    private final ObjectMapper objectMapper;

    // 알림 조회 (유조별)
    public List<AlarmResponseDto> getAlarmsByUser(String username) {
        List<Alarm> findAlarms = alarmRepository.getAlarmByUsers_Username(username);

        Long notViewedAlarmCnt = alarmRepository.getNotViewedAlarmCnt(username);

        List<AlarmResponseDto> alarmResponseDtos = new ArrayList<>();
        for (Alarm findAlarm : findAlarms) {
            try{
                // JSON 변환
                Map<String, String> metaMap = objectMapper.readValue(findAlarm.getMetadata(), new TypeReference<>() {});

                String content = renderTemplate(metaMap, findAlarm.getAlarmType().getTemplate());

                String link = createLink(metaMap);

                String postIdString = metaMap.get("postId");
                Long postId = null;
                if (postIdString != null && !postIdString.isEmpty()) {
                    postId = Long.valueOf(postIdString);
                }

                AlarmResponseDto alarmResponseDto =
                    new AlarmResponseDto(
                        findAlarm.getId(),
                        content,
                        findAlarm.getIsViewed(),
                        postId,
                        link,
                        notViewedAlarmCnt
                    );

                alarmResponseDtos.add(alarmResponseDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }

        return alarmResponseDtos;
    }

    // 하나의 String 만들기
    public String renderTemplate (Map<String, String> metamap, String template) {
        for (Entry<String, String> entry : metamap.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return template;
    }

    // 링크(웹 프론트) 생성
    public String createLink(Map<String, String> metamap) {
        String postTitle = metamap.get("postTitle");
        String targetCommentId = metamap.get("targetCommentId");
        String postId = metamap.get("postId");

        if (postTitle == null || postTitle.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("/post/");
        sb.append(postId).append("/");
        sb.append(postTitle);

        if (targetCommentId != null && !targetCommentId.isEmpty()) {
            sb.append("#comment").append(targetCommentId);
        }

        return sb.toString();
    }

    // 알림 생성
    public Long createAlarm(AlarmRequestDto alarmRequestDto) {
        Users findUser = usersRepository.findUsersByUsername(alarmRequestDto.getUsername())
            .orElseThrow();

        List<AlarmType> alarmTypes = alarmTypeRepository.findByType(alarmRequestDto.getAlarmType());
        AlarmType findAlarmType = alarmTypes.getFirst();

        String newMetaData = alarmRequestDto.getMetadata();

        Alarm newAlarm = Alarm.createAlarm(findUser, findAlarmType, newMetaData);

        alarmRepository.save(newAlarm);

        return newAlarm.getId();
    }

    // 알람 읽기 처리
    @Transactional
    public Long viewAlarm(Long alarmId) {
        Alarm findAlarm = alarmRepository.getAlarmById(alarmId);

        findAlarm.hasViewed();

        return findAlarm.getId();
    }

    @Transactional
    public void deleteAlarm(Long alarmId){
        alarmRepository.deleteById(alarmId);
    }

    // 알림 생성
    public void createAlarm(String username, AlarmTypeEnum type, Map<String, String> meta) {
        try {
            String metadata = objectMapper.writeValueAsString(meta);
            AlarmRequestDto comment = new AlarmRequestDto(metadata, type, username);

            createAlarm(comment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteAllAlarmByUsername(String username) {
        alarmRepository.bulkDeleteAlarmsByUsername(username);
    }

    @Transactional
    public void updateAlarmIsViewedTrueByUsername(String username) {
        alarmRepository.bulkUpdateAlarmIsViewTrue(username);
    }
}
