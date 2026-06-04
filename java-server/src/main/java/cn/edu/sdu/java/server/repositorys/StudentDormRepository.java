package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.StudentDorm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentDormRepository extends JpaRepository<StudentDorm, Integer> {
    List<StudentDorm> findByDormIdAndStatus(Integer dormId, Integer status);
    StudentDorm findByStudentIdAndStatus(Integer studentId, Integer status);
}