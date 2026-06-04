package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "volunteer_activity")
public class VolunteerActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 100)
    private String name;                    // 志愿活动名称

    @NotBlank
    @Size(max = 200)
    private String location;                // 活动地点

    private LocalDate activityDate;         // 活动日期

    private LocalTime startTime;            // 开始时间

    private LocalTime endTime;              // 结束时间

    @Column(columnDefinition = "TEXT")
    private String workDescription;         // 志愿工作内容

    private Integer recruitCount = 0;       // 招募人数

    private BigDecimal volunteerHours;      // 志愿时长(小时)

    @Column(columnDefinition = "TEXT")
    private String requirements;            // 活动要求

    @Column(columnDefinition = "TEXT")
    private String notes;                   // 注意事项

    private LocalDateTime signupStart;      // 报名开始时间

    private LocalDateTime signupEnd;        // 报名截止时间

    @Column(length = 10)
    private String status = "PENDING";      // PENDING, ONGOING, FINISHED

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}