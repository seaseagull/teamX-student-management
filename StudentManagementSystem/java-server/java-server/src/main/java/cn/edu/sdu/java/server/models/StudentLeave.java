package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(	name = "student_leave",
        uniqueConstraints = {
        })
public class StudentLeave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentLeaveId;

    @ManyToOne
    @JoinColumn(name="studentId")
    private Student student;

    @ManyToOne
    @JoinColumn(name="teacherId")
    private Teacher teacher;

    @Size(max=50)
    private String leaveDate;
    @Size(max=50)
    private String returnDate;
    @Size(max=100)
    private String reason;
    private Integer state;
    private Date applyTime;
    @Size(max=100)
    private String teacherComment;
    private Date teacherTime;
    private Boolean teacherChecked = false;  // 教师是否已审核
    private Boolean teacherPass = false;      // 教师是否通过
    @Size(max=100)
    private String adminComment;
    private Date adminTime;
    private Boolean adminChecked = false;     // 管理员是否已审核
    private Boolean adminPass = false;       // 管理员是否通过
}