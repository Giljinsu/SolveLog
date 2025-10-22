package com.study.blog;

import com.study.blog.entity.AlarmType;
import com.study.blog.entity.Category;
import com.study.blog.entity.enums.AlarmTypeEnum;
import com.study.blog.repository.AlarmTypeRepository;
import com.study.blog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 사용 x 대신 flyway 사용중

@Profile("prod")
@Component
@RequiredArgsConstructor
public class InitProdData {
    private final CategoryRepository categoryRepository;
    private final AlarmTypeRepository alarmTypeRepository;

//    @EventListener(ApplicationReadyEvent.class)
//    @Transactional
//    public void init() {
//        //alarm
//        AlarmType alarmTypeComment = AlarmType.createAlarmType(AlarmTypeEnum.COMMENT, "{{sender}}님이 '{{postTitle}}'에 댓글을 남겼습니다.");
//        AlarmType alarmTypeLike = AlarmType.createAlarmType(AlarmTypeEnum.LIKE, "{{sender}}님이 {{postTitle}}에 좋아요를 눌렀습니다."); //~님 title에 좋아요를 눌렀습니다
//        AlarmType alarmTypeReply = AlarmType.createAlarmType(AlarmTypeEnum.REPLY, "{{sender}}님이 회원님의 댓글에 답글을 남겼습니다.");
//
//        alarmTypeRepository.save(alarmTypeComment);
//        alarmTypeRepository.save(alarmTypeReply);
//        alarmTypeRepository.save(alarmTypeLike);
//
//        //category
//        Category parentCategory = Category.createCategory("SEARCH_CATEGORY");
//        Category solveCategory = Category.createCategory("문제풀이",parentCategory);
//        Category freeCategory = Category.createCategory("자유게시판", parentCategory);
//
//        categoryRepository.save(parentCategory);
//        categoryRepository.save(solveCategory);
//        categoryRepository.save(freeCategory);
//
//
//    }

}
