package com.example.final_project.src.funding.model;

import com.example.final_project.src.funding.entity.Funding;
import com.example.final_project.src.user.entity.User;
import lombok.*;

import javax.sql.rowset.serial.SerialClob;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.*;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FundingInsertReq {
    private String title;
    private String subtitle;
    private Long category_id;
    private String writer;
    private String location;
    private Date deadline;
    private Long goal;
    private Long min_participants;
    private Long max_participants;
    private Long amount;
    private String thumbnail;
    private String content;


    public Funding toEntity() throws SQLException {
        return Funding.builder()
                .title(this.title)
                .subtitle(this.subtitle)
                .location(isNullLocation(this.location))
                .deadline(convertDateToLocalDateTime(this.deadline)) // Date를 LocalDateTime으로 변환
                .goal(this.goal)
                .min_participants(this.min_participants)
                .max_participants(this.max_participants)
                .amount(this.amount)
                .thumbnail(this.thumbnail)
                .content(this.content)
                .build();
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

    public String isNullLocation(String location) {
        if(location == null || location.isEmpty()) {
            return "ONLINE";
        }
        else {
            return location;
        }
    }

}
