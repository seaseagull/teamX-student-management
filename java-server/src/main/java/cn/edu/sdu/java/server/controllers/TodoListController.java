package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.TodoListService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/todo")
public class TodoListController {
    private final TodoListService todoListService;

    public TodoListController(TodoListService todoListService) {
        this.todoListService = todoListService;
    }

    @PostMapping("/getTodoList")
    public DataResponse getTodoList(@Valid @RequestBody DataRequest dataRequest) {
        return todoListService.getTodoList(dataRequest);
    }

    @PostMapping("/todoSave")
    public DataResponse todoSave(@Valid @RequestBody DataRequest dataRequest) {
        return todoListService.todoSave(dataRequest);
    }

    @PostMapping("/todoDelete")
    public DataResponse todoDelete(@Valid @RequestBody DataRequest dataRequest) {
        return todoListService.todoDelete(dataRequest);
    }
}