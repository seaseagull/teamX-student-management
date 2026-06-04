package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.StudentLeave;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.StudentLeaveRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentLeaveService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentLeaveRepository studentLeaveRepository;

    public StudentLeaveService(StudentRepository studentRepository, TeacherRepository teacherRepository, StudentLeaveRepository studentLeaveRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.studentLeaveRepository = studentLeaveRepository;
    }

    /**
     * 请假申请中「指导老师」下拉选项：{@code value} 为教师 {@code personId} 供保存；展示文案仅为姓名，不含工号/登录名。
     */
    public OptionItemList getTeacherItemOptionList(DataRequest dataRequest) {
        List<Teacher> sList = teacherRepository.findAll();
        List<OptionItem> itemList = new ArrayList<>();
        for (Teacher t : sList) {
            itemList.add(new OptionItem(t.getPersonId(), t.getPersonId() + "", formatTeacherDisplayName(t)));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getStudentLeaveList(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        String userName = CommonMethod.getUsername();
        Integer filterState = dataRequest.getInteger("state");  // 修改变量名
        if(filterState == null)
            filterState = -1;
        String search = dataRequest.getString("search");
        assert roleName != null;
        List<StudentLeave> slList = switch (roleName) {
            case "ROLE_STUDENT" -> studentLeaveRepository.getStudentLeaveList(-1, search, userName, "");
            case "ROLE_TEACHER" -> studentLeaveRepository.getStudentLeaveList(-1, search, "", userName);
            case "ROLE_ADMIN" -> studentLeaveRepository.getStudentLeaveList(-1, search, "", "");  // 不再传递state给Repository
            default -> null;
        };
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map;
        Student s;
        Teacher t;
        ComDataUtil di = ComDataUtil.getInstance();
        if (slList != null && !slList.isEmpty()) {
            for (StudentLeave sl : slList) {
                // ========== 先计算当前记录的状态 ==========
                Integer currentState;
                boolean teacherChecked = Boolean.TRUE.equals(sl.getTeacherChecked());
                boolean adminChecked = Boolean.TRUE.equals(sl.getAdminChecked());

                if (!teacherChecked && !adminChecked) {
                    currentState = 0;  // 待审核（双方都没审核）
                } else if (teacherChecked && adminChecked) {
                    // 双方都审核完成，判断通过与否
                    if (Boolean.TRUE.equals(sl.getTeacherPass()) && Boolean.TRUE.equals(sl.getAdminPass())) {
                        currentState = 2;  // 审核通过
                    } else {
                        currentState = 3;  // 审核不通过
                    }
                } else {
                    currentState = 1;  // 审核中（有一方已审核但还没全部审核完）
                }
                
                // ========== 根据过滤条件筛选 ==========
                if (filterState != -1 && !filterState.equals(currentState)) {
                    continue;  // 不匹配则跳过
                }
                
                map = new HashMap<>();
                s = sl.getStudent();
                t = sl.getTeacher();
                map.put("studentLeaveId", sl.getStudentLeaveId());
                map.put("studentNum", s.getPerson().getNum());
                map.put("studentName", s.getPerson().getName());
                map.put("studentId", s.getPersonId());
                map.put("teacherName", formatTeacherDisplayName(t));
                map.put("state", currentState);  // 使用计算后的状态
                map.put("reason", sl.getReason());
                map.put("leaveDate", sl.getLeaveDate());
                map.put("returnDate", sl.getReturnDate());
                map.put("adminComment", sl.getAdminComment());
                map.put("adminChecked", sl.getAdminChecked());
                map.put("adminPass", sl.getAdminPass());
                map.put("teacherId", t != null ? t.getPersonId() : null);
                map.put("teacherComment", sl.getTeacherComment());
                map.put("teacherChecked", sl.getTeacherChecked());
                map.put("teacherPass", sl.getTeacherPass());

                // ========== 设置状态显示名称 ==========
                String stateName;
                if (currentState == 0) {
                    stateName = "待审核";
                } else if (currentState == 1) {
                    stateName = "审核中";
                } else if (currentState == 2) {
                    stateName = "审核通过";
                } else {
                    stateName = "审核不通过";
                }
                map.put("stateName", stateName);
                dataList.add(map);
            }
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse studentLeaveSave(DataRequest dataRequest) {
        Integer state = dataRequest.getInteger("state");
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        Integer teacherId = dataRequest.getInteger("teacherId");
        String leaveDate = dataRequest.getString("leaveDate");
        String returnDate = dataRequest.getString("returnDate");
        String reason = dataRequest.getString("reason");

        if (leaveDate == null || leaveDate.trim().isEmpty()) {
            return CommonMethod.getReturnMessageError("离校日期不能为空！");
        }
        if (returnDate == null || returnDate.trim().isEmpty()) {
            return CommonMethod.getReturnMessageError("返校日期不能为空！");
        }

        java.time.LocalDate leaveLocalDate;
        java.time.LocalDate returnLocalDate;
        try {
            leaveLocalDate = java.time.LocalDate.parse(leaveDate);
            returnLocalDate = java.time.LocalDate.parse(returnDate);
        } catch (java.time.format.DateTimeParseException e) {
            return CommonMethod.getReturnMessageError("日期格式不正确，请使用yyyy-MM-dd格式！");
        }

        if (returnLocalDate.isBefore(leaveLocalDate)) {
            return CommonMethod.getReturnMessageError("返校日期不能早于离校日期！");
        }

        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isPresent())
                sl = op.get();
            if(sl != null && (Boolean.TRUE.equals(sl.getTeacherChecked()) || Boolean.TRUE.equals(sl.getAdminChecked()))) {
                return CommonMethod.getReturnMessageError("该请假申请已审核，无法修改！");
            }
        }
        if(sl == null) {
            sl = new StudentLeave();
            sl.setState(0);
            sl.setApplyTime(new Date());
            sl.setTeacherComment("");
            sl.setAdminComment("");
            sl.setStudent(studentRepository.findByPersonNum(CommonMethod.getUsername()).get());
            sl.setTeacherChecked(false);
            sl.setTeacherPass(false);
            sl.setAdminChecked(false);
            sl.setAdminPass(false);
        }
        if(teacherId != null && teacherId > 0) {
            Optional<Teacher> op = teacherRepository.findById(teacherId);
            if(op.isPresent())
                sl.setTeacher(op.get());
        }
        sl.setLeaveDate(leaveDate);
        sl.setReturnDate(returnDate);
        sl.setReason(reason);
        sl.setState(state);
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }
    public DataResponse studentLeaveCheck(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        Integer state = dataRequest.getInteger("state");//// 0=通过, 1=不通过（从前端传入）
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        String teacherComment = dataRequest.getString("teacherComment");
        String adminComment = dataRequest.getString("adminComment");
        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isPresent())
                sl = op.get();
        }
        if(sl == null) {
            return CommonMethod.getReturnMessageOK();
        }
        if("ROLE_ADMIN".equals(roleName)) {
            // ========== 管理员审核逻辑 ==========
            sl.setAdminComment(adminComment);
            sl.setAdminTime(new Date());
            //设置管理员审核状态
            sl.setAdminChecked(true);
            sl.setAdminPass(state == 0);  // state=0表示通过，state=1表示不通过
        } else if("ROLE_TEACHER".equals(roleName)) {
            // ========== 教师审核逻辑 ==========
            sl.setTeacherComment(teacherComment);
            sl.setTeacherTime(new Date());
            //设置教师审核状态
            sl.setTeacherChecked(true);
            sl.setTeacherPass(state == 0);  // state=0表示通过，state=1表示不通过
        }
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }

    /**
     * 教师在下拉与列表中的展示名：优先姓名，否则用工号；教师或人员为空时返回空串。
     *
     * @param teacher 教师实体，可为 {@code null}
     * @return 展示用字符串
     */
    private static String formatTeacherDisplayName(Teacher teacher) {
        if (teacher == null || teacher.getPerson() == null) {
            return "";
        }
        String name = teacher.getPerson().getName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        String num = teacher.getPerson().getNum();
        return num != null ? num : "";
    }
}