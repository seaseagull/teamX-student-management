package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.ExamService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/exam")
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/getExamList")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getExamList(@Valid @RequestBody DataRequest dataRequest) {
        return examService.getExamList(dataRequest);
    }

    @PostMapping("/getExamInfo")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getExamInfo(@Valid @RequestBody DataRequest dataRequest) {
        return examService.getExamInfo(dataRequest);
    }

    @PostMapping("/examEditSave")
    @PreAuthorize(" hasRole('ADMIN')")
    public DataResponse examEditSave(@Valid @RequestBody DataRequest dataRequest) {
        return examService.examEditSave(dataRequest);
    }

    @PostMapping("/examDelete")
    @PreAuthorize(" hasRole('ADMIN')")
    public DataResponse examDelete(@Valid @RequestBody DataRequest dataRequest) {
        return examService.examDelete(dataRequest);
    }
}