package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class LostGoods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String createTime;
    private String status;
}