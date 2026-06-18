package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    List<Notice> findAllByOrderByCreateTimeDesc();
}