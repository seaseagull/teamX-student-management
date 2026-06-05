package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_dormitory")
@Data
public class Dormitory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 统一用Integer主键

    @Column(name = "building_no", nullable = false)
    private String buildingNo;

    @Column(name = "room_no", nullable = false)
    private String roomNo;

    @Column(name = "bed_count")
    private Integer bedCount;

    @Column(name = "used_bed")
    private Integer usedBed;

    private Integer status; // 1-正常 0-停用

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}