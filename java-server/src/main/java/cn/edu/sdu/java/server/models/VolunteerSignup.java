package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "volunteer_signup",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"activity_id", "student_id"})
        })
public class VolunteerSignup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private VolunteerActivity activity;     // 关联活动

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "person_id")
    private Student student;                 // 关联学生

    private LocalDateTime signupTime;       // 报名时间

    private BigDecimal hoursEarned = BigDecimal.ZERO;  // 获得志愿时长

    @Column(length = 10)
    private String status = "SIGNED";       // SIGNED, COMPLETED, CANCELLED

    @PrePersist
    protected void onCreate() {
        signupTime = LocalDateTime.now();
    }
}