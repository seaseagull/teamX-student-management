package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Homework;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Course 数据操作接口，主要实现Course数据的查询操作
 */

@Repository
public interface CourseRepository extends JpaRepository<Course,Integer> {
    @Query(value = "from Course where ?1='' or num like %?1% or name like %?1% ")
    List<Course> findCourseListByNumName(String numName);

    Optional<Course> findByNum(String num);
    List<Course> findByName(String name);

    @Query("SELECT c FROM Course c WHERE" +
            "(:search = '' OR c.name LIKE %:search%) " +
            "AND (:state = -1 OR c.credit = :state) " +
            "AND (:teacherId = -1 OR c.teacher.personId = :teacherId)")
    List<Course> getCourseList(@Param("search") String search,
                                   @Param("credit") Integer state,
                                   @Param("teacherId") Integer teacherId);
}
