package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_student_dorm")
@Data
public class StudentDorm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 统一用Integer主键

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "dorm_id", nullable = false)
    private Integer dormId;

    @Column(name = "bed_no")
    private String bedNo;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    private Integer status; // 1-在住 0-已退宿

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}