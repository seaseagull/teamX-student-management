package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/leave")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/getList")
    public DataResponse getLeaveRequestList(@Valid @RequestBody DataRequest dataRequest) {
        return leaveRequestService.getLeaveRequestList(dataRequest);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse saveLeaveRequest(@Valid @RequestBody DataRequest dataRequest) {
        return leaveRequestService.saveLeaveRequest(dataRequest);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse approveLeaveRequest(@Valid @RequestBody DataRequest dataRequest) {
        return leaveRequestService.approveLeaveRequest(dataRequest);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public DataResponse deleteLeaveRequest(@Valid @RequestBody DataRequest dataRequest) {
        return leaveRequestService.deleteLeaveRequest(dataRequest);
    }
}