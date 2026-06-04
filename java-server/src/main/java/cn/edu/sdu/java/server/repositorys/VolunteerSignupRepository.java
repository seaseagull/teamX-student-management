package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.VolunteerSignup;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VolunteerSignupRepository extends JpaRepository<VolunteerSignup, Integer> {

    // 查某活动的报名人数
    long countByActivityId(Integer activityId);

    // 查某活动的所有报名记录
    List<VolunteerSignup> findByActivityId(Integer activityId);

    // 查某学生是否已报某活动
    boolean existsByActivityIdAndStudentPersonId(Integer activityId, Integer studentId);

    // 查某学生的所有报名记录
    List<VolunteerSignup> findByStudentPersonIdOrderBySignupTimeDesc(Integer studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VolunteerSignup s WHERE s.activity.id = :activityId AND s.student.personId = :studentId")
    void cancelSignup(@Param("activityId") Integer activityId, @Param("studentId") Integer studentId);
}