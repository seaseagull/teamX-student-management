package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    // 根据学生ID查询
    List<LeaveRequest> findByStudentPersonIdOrderByCreateTimeDesc(Integer studentId);

    // 根据状态查询（管理员用）
    List<LeaveRequest> findByStatusOrderByCreateTimeDesc(Integer status);

    // 查询所有（管理员用）
    List<LeaveRequest> findAllByOrderByCreateTimeDesc();

    // 查询某个班级的申请（教师用）
    @Query("SELECT l FROM LeaveRequest l WHERE l.student.className = ?1 ORDER BY l.createTime DESC")
    List<LeaveRequest> findByClassName(String className);
}