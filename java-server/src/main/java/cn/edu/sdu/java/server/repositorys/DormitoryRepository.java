package cn.edu.sdu.java.server.repositorys; // 你的包名是repositorys，不是repositories

import cn.edu.sdu.java.server.models.Dormitory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DormitoryRepository extends JpaRepository<Dormitory, Integer> {
    // 泛型第二个参数必须是Integer，和主键类型一致
    List<Dormitory> findByStatus(Integer status);
}