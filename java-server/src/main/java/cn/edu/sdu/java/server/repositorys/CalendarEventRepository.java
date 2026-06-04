package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByDateBetween(LocalDate start, LocalDate end);
}