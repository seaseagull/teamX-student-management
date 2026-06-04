package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.TodoList;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.TodoListRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TodoListService {
    private final TodoListRepository todoListRepository;
    private final PersonRepository personRepository;

    public TodoListService(TodoListRepository todoListRepository, PersonRepository personRepository) {
        this.todoListRepository = todoListRepository;
        this.personRepository = personRepository;
    }

    // 2. 修改 getPersonIdByUsername 方法
    private Integer getPersonIdByUsername(DataRequest dataRequest) {
        System.out.println("【DEBUG】getTodoList 被调用了");
        System.out.println("【DEBUG】data = " + dataRequest.getData());
        String username = dataRequest.getString("username");
        System.out.println("【DEBUG】后端收到的 username = " + username);
        if (username == null || username.isEmpty()) return 0;
        Optional<Person> op = personRepository.findByNum(username);
        System.out.println("【DEBUG】查到的 personId = " + (op.isPresent() ? op.get().getPersonId() : "未找到"));
        if (op.isPresent()) {
            return op.get().getPersonId();
        }
        return 0;
    }

    public DataResponse getTodoList(DataRequest dataRequest) {
        Integer personId = getPersonIdByUsername(dataRequest); // ← 改这里
        List<TodoList> list = todoListRepository.findByPersonIdOrderByCreateTimeDesc(personId);
        if (personId == null) personId = 0;
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (TodoList t : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("content", t.getContent());
            map.put("time", t.getTime());
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse todoSave(DataRequest dataRequest) {
        Integer personId = getPersonIdByUsername(dataRequest); // ← 改这里
        String content = dataRequest.getString("content");
        String time = dataRequest.getString("time");
        TodoList todo = new TodoList();
        todo.setPersonId(personId);
        todo.setContent(content);
        todo.setTime(time);
        todoListRepository.save(todo);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse todoDelete(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        if (id != null) {
            todoListRepository.deleteById(id);
        }
        return CommonMethod.getReturnMessageOK();
    }
}