package com.study.blog.entity;

import com.study.blog.entity.basicentity.BasicDate;
import com.study.blog.entity.enums.StatisticType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class UserStatistic extends BasicDate {

    // 이 엔티티는 spring batch에 의해서만 데이터를 넣어주자
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_statistic_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatisticType statisticType; // TOTAL, YEAR, MONTH, DAILY, TAG, CATEGORY

    // 통계의 기준 시간
    /*
        TOTAL  → null
        YEAR   → 해당 연도 1월 1일
        MONTH  → 해당 월 1일
        DAILY  → 해당 날짜
        TAG    → null
        CATEGORY → null
     */
    private LocalDate statisticDate;

    private String categoryName;
    private String tagName;
//    private String targetName; // 태그명, 카테고리명

    private Long statisticCount;

    private UserStatistic(Users user, StatisticType statisticType) {
        this.user = user;
        this.statisticType = statisticType;
    }

    //생성 메서드
    public static UserStatistic createUserStatistic(Users user, StatisticType statisticType) {
        return new UserStatistic(user, statisticType);
    }

//    public void updateUserStatistic(String targetName, Long count, LocalDate statisticDate) {
//        setTargetName(targetName);
//        setCount(count);
//        setStatisticDate(statisticDate);
//    }

    public void updateUserStatistic(String categoryName, String tagName, Long statisticCount, LocalDate statisticDate) {
        setCategoryName(categoryName);
        setTagName(tagName);
        setStatisticCount(statisticCount);
        setStatisticDate(statisticDate);
    }
}
