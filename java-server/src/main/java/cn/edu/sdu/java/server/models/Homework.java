package cn.edu.sdu.java.server.models;


/*
 * Homework 作业实体类  保存作业的的基本信息信息，
 * Integer homeworkId 人员表 homework 主键 homework_id
 * String name 作业名称
 * Course course 所属课程 关联课程表的主键 course_id
 * Date deadline 截止日期
 * Boolean isCompleted 是否完成
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(	name = "homework",
        uniqueConstraints = {
        })
public class Homework  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer homeworkId;

    @NotBlank
    @Size(max = 50)
    private String name;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name="course_id")
    private Course course;

    @Size(max=50)
    private String deadline;

    @Size(max=100)
    private String remark;
    private Integer state;

}
