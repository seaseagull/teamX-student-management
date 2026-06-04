package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DormInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String buildingNo;
    private String roomNo;
    private Integer bedCount;
    private Integer usedBed;
    private Integer emptyBed;
}