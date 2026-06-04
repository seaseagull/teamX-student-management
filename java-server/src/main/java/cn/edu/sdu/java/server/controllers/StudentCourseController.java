package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.services.HomeworkService;
import cn.edu.sdu.java.server.services.StudentCourseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/studentCourse")
public class StudentCourseController {
    private final StudentCourseService studentCourseService;


    @PostMapping("/getTeacherItemOptionList")
    @PreAuthorize("hasRole('STUDENT')")
    public OptionItemList getTeacherItemOptionList(@Valid @RequestBody DataRequest dataRequest) {
        return studentCourseService.getTeacherItemOptionList(dataRequest);
    }

    @PostMapping("/getCourseList")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')  or hasRole('TEACHER')")
    public DataResponse getCourseList(@Valid @RequestBody DataRequest dataRequest) {
        return studentCourseService.getCourseList(dataRequest);
    }

    @PostMapping("/getStudentCourses")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getStudentCourses(@Valid @RequestBody DataRequest dataRequest) {
        return studentCourseService.getStudentCourses(dataRequest);
    }

    @PostMapping("/courseSelect")
    @PreAuthorize("hasRole('STUDENT') ")
    public DataResponse courseSelect(@Valid @RequestBody DataRequest dataRequest) {
        return studentCourseService.courseSelect(dataRequest);
    }

    @PostMapping("/courseCancel")
    @PreAuthorize("hasRole('STUDENT') ")
    public DataResponse courseCancel(@Valid @RequestBody DataRequest dataRequest) {
        return studentCourseService.courseCancel(dataRequest);
    }
}