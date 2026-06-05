package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DormAllot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stuName;    // 对应库：stu_name
    private String className;  // 对应库：class_name
    private String dormName;   // 对应库：dorm_name
    private Integer bedNo;     // 对应库：bed_no
    private String state;     // 对应库：state
}