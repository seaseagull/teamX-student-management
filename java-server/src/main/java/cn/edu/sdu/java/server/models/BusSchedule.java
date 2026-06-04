package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bus_schedule")
public class BusSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 50)
    @Column(name = "from_campus")
    private String fromCampus;

    @Size(max = 50)
    @Column(name = "to_campus")
    private String toCampus;

    @Size(max = 20)
    @Column(name = "schedule_type")
    private String scheduleType;

    @Size(max = 10)
    @Column(name = "departure_time")
    private String departureTime;
}