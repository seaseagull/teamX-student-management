package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.StudentCourse;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Integer> {
    // 查某学生的所有选课
    List<StudentCourse> findByStudentPersonId(Integer studentId);

    @Transactional
    void deleteByStudentPersonIdAndCourseCourseId(Integer studentId, Integer courseId);
}