package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    @Query(value = "from Exam where ?1='' or examName like %?1%",
            countQuery = "select count(examId) from Exam where ?1='' or examName like %?1%")
    Page<Exam> findExamPageByName(String examName, Pageable pageable);
}