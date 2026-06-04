package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.LeaveRequest;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.LeaveRequestRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, StudentRepository studentRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.studentRepository = studentRepository;
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 获取离校申请列表
    public DataResponse getLeaveRequestList(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        Integer status = dataRequest.getInteger("status");

        List<LeaveRequest> requestList = new ArrayList<>();

        if ("ROLE_ADMIN".equals(roleName)) {
            // 管理员看到所有
            if (status != null && status >= 0) {
                requestList = leaveRequestRepository.findByStatusOrderByCreateTimeDesc(status);
            } else {
                requestList = leaveRequestRepository.findAllByOrderByCreateTimeDesc();
            }
        } else if ("ROLE_TEACHER".equals(roleName)) {
            // 教师看到所有（可改为按班级筛选）
            requestList = leaveRequestRepository.findAllByOrderByCreateTimeDesc();
        } else {
            // 学生看到自己的
            Integer personId = CommonMethod.getPersonId();
            if (personId != null) {
                requestList = leaveRequestRepository.findByStudentPersonIdOrderByCreateTimeDesc(personId);
            }
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (LeaveRequest request : requestList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", request.getId());
            map.put("studentId", request.getStudent().getPersonId());
            map.put("studentNum", request.getStudent().getPerson().getNum());
            map.put("studentName", request.getStudent().getPerson().getName());
            map.put("className", request.getStudent().getClassName());
            map.put("startTime", request.getStartTime() != null ? request.getStartTime().format(formatter) : "");
            map.put("endTime", request.getEndTime() != null ? request.getEndTime().format(formatter) : "");
            map.put("reason", request.getReason());
            map.put("status", request.getStatus());
            map.put("adminComment", request.getAdminComment());
            map.put("createTime", request.getCreateTime() != null ? request.getCreateTime().format(formatter) : "");
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }

    // 提交/保存离校申请
    public DataResponse saveLeaveRequest(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        String startTimeStr = dataRequest.getString("startTime");
        String endTimeStr = dataRequest.getString("endTime");
        String reason = dataRequest.getString("reason");

        LeaveRequest request;
        if (id != null && id > 0) {
            Optional<LeaveRequest> op = leaveRequestRepository.findById(id);
            if (op.isPresent()) {
                request = op.get();
            } else {
                request = new LeaveRequest();
            }
        } else {
            request = new LeaveRequest();
            Integer personId = CommonMethod.getPersonId();
            Optional<Student> studentOp = studentRepository.findById(personId);
            if (studentOp.isPresent()) {
                request.setStudent(studentOp.get());
            } else {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            request.setCreateTime(LocalDateTime.now());
            request.setStatus(0);
        }

        request.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
        request.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
        request.setReason(reason);
        request.setUpdateTime(LocalDateTime.now());

        leaveRequestRepository.save(request);
        return CommonMethod.getReturnMessageOK();
    }

    // 审批离校申请
    public DataResponse approveLeaveRequest(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        Integer status = dataRequest.getInteger("status");
        String comment = dataRequest.getString("comment");

        Optional<LeaveRequest> op = leaveRequestRepository.findById(id);
        if (op.isEmpty()) {
            return CommonMethod.getReturnMessageError("申请不存在");
        }

        LeaveRequest request = op.get();
        request.setStatus(status);
        request.setAdminComment(comment);
        request.setUpdateTime(LocalDateTime.now());
        leaveRequestRepository.save(request);

        return CommonMethod.getReturnMessageOK();
    }

    // 删除离校申请
    public DataResponse deleteLeaveRequest(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        leaveRequestRepository.deleteById(id);
        return CommonMethod.getReturnMessageOK();
    }
}