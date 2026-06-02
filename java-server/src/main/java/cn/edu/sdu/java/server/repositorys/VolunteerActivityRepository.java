package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.VolunteerActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VolunteerActivityRepository extends JpaRepository<VolunteerActivity, Integer> {
    List<VolunteerActivity> findAllByOrderByCreatedAtDesc();
}