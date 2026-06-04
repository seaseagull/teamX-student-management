package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BusSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusScheduleRepository extends JpaRepository<BusSchedule, Integer> {
    List<BusSchedule> findByFromCampusAndToCampusAndScheduleTypeOrderByDepartureTimeAsc(
            String fromCampus, String toCampus, String scheduleType);
}