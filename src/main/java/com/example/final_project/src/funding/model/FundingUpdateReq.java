package com.example.final_project.src.funding.model;

import com.example.final_project.src.funding.entity.Funding;
import lombok.*;

import java.sql.SQLException;
import java.time.*;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FundingUpdateReq {
    private String username;
    private String title;
    private String subtitle;
    private Long category_id;
    private String location;
    private Date deadline;
    private Long goal;
    private Long min_participants;
    private Long max_participants;
    private Long amount;
    private String thumbnail;
    private String content;


    public FundingUpdateReq(String username, String title, String subtitle, Long category_id, String location, LocalDateTime deadline, Long goal, Long min_participants, Long max_participants, Long amount, String thumbnail, String content) {
        this.username = username;
        this.title = title;
        this.subtitle = subtitle;
        this.category_id = category_id;
        this.location = isNullLocation(location);
        this.deadline = convertLocalDateTimeToDate(deadline);
        this.goal = goal;
        this.min_participants = min_participants;
        this.max_participants = max_participants;
        this.amount = amount;
        this.thumbnail = thumbnail;
        this.content = content;
    }


    // Date를 LocalDateTime으로 변환하는 메서드
    private static LocalDateTime convertDateToLocalDateTime(Date date) {
        // Date 객체를 LocalDate와 LocalTime으로 분리
        Instant instant = date.toInstant();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDate localDate = zonedDateTime.toLocalDate().plusDays(1); // 다음 날로 설정
        // 다음 날의 자정 (00:00:00)으로 LocalDateTime 생성
        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }

    private static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        // LocalDateTime을 Date로 변환
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    public String isNullLocation(String location) {
        if(location == null || location.isEmpty()) {
            return "ONLINE";
        }
        else {
            return location;
        }
    }

}
