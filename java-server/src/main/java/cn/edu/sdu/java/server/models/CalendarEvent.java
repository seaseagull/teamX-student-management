package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "calendar_event")
@Data
public class CalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 事件名称
    private LocalDate date;    // 事件日期
    private String type;       // 类型
    private String color;      // 显示颜色

}