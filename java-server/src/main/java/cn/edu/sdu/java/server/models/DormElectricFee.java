package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class DormElectricFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String buildingNo;
    private String roomNo;
    private BigDecimal fee;
    private String payTime;
    private String status;
}