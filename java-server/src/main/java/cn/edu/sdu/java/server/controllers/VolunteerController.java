package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.VolunteerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {
    private final VolunteerService volunteerService;

    // ==================== 活动 ====================

    @PostMapping("/getActivityList")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public DataResponse getActivityList(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.getActivityList(dataRequest);
    }

    @PostMapping("/activitySave")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse activitySave(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.activitySave(dataRequest);
    }

    @PostMapping("/changeStatus")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse changeStatus(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.changeStatus(dataRequest);
    }

    // ==================== 报名 ====================

    @PostMapping("/signup")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse signup(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.signup(dataRequest);
    }

    @PostMapping("/cancelSignup")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse cancelSignup(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.cancelSignup(dataRequest);
    }

    @PostMapping("/checkSignup")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse checkSignup(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.checkSignup(dataRequest);
    }

    @PostMapping("/getStudentActivityList")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getStudentActivityList(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.getStudentActivityList(dataRequest);
    }

    // ==================== 志愿者管理 ====================

    @PostMapping("/getVolunteers")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getVolunteers(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.getVolunteers(dataRequest);
    }

    @PostMapping("/volunteerDelete")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse volunteerDelete(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.volunteerDelete(dataRequest);
    }

    @PostMapping("/deleteActivity")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse deleteActivity(@Valid @RequestBody DataRequest dataRequest) {
        return volunteerService.deleteActivity(dataRequest);
    }
}