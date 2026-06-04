package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoListRepository extends JpaRepository<TodoList, Integer> {
    List<TodoList> findByPersonIdOrderByCreateTimeDesc(Integer personId);
}