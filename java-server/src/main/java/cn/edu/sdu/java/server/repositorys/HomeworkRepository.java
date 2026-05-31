package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Homework;
import cn.edu.sdu.java.server.models.StudentLeave;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework,Integer> {

    @Query("SELECT h FROM Homework h WHERE h.student.personId = :studentId " +
            "AND (:search = '' OR h.name LIKE %:search%) " +
            "AND (:state = -1 OR h.state = :state) " +
            "AND (:courseId = -1 OR h.course.courseId = :courseId)")
    List<Homework> getHomeworkList(@Param("studentId") Integer studentId,
                                   @Param("search") String search,
                                   @Param("state") Integer state,
                                   @Param("courseId") Integer courseId);
}
