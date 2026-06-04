package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.DormitoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/personnel/dormitory") // 统一加/api前缀，和其他Controller保持一致
public class DormitoryController {

    private final DormitoryService dormitoryService;

    // 构造器注入（和你所有Controller完全一致）
    public DormitoryController(DormitoryService dormitoryService) {
        this.dormitoryService = dormitoryService;
    }

    @PostMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public DataResponse list(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryService.getDormitoryList(dataRequest);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')") // 只有管理员和老师能新增/编辑
    public DataResponse save(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryService.dormitorySave(dataRequest);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')") // 只有管理员能删除
    public DataResponse delete(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryService.dormitoryDelete(dataRequest);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse assign(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryService.assignStudent(dataRequest);
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse checkout(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryService.checkOutStudent(dataRequest);
    }

    @PostMapping("/studentList")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public DataResponse studentList(@Valid @RequestBody DataRequest dataRequest) {
        return dormitoryService.getStudentListInDorm(dataRequest);
    }
}