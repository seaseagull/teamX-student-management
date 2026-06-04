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
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentLeaveService {
    private static final int REASON_MAX_LENGTH = 100;
    /** 列表筛选：草稿（仅学生端使用，教师/管理员不可见草稿记录） */
    public static final int FILTER_STATE_DRAFT = 4;

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
        Integer filterState = dataRequest.getInteger("state");
        if(filterState == null)
            filterState = -1;
        String search = dataRequest.getString("search");
        if (search == null) {
            search = "";
        }
        assert roleName != null;
        boolean studentRole = "ROLE_STUDENT".equals(roleName);
        List<StudentLeave> slList = switch (roleName) {
            case "ROLE_STUDENT" -> studentLeaveRepository.getStudentLeaveList(search, userName, "");
            case "ROLE_TEACHER" -> studentLeaveRepository.getStudentLeaveList(search, "", userName);
            case "ROLE_ADMIN" -> studentLeaveRepository.getStudentLeaveList(search, "", "");
            default -> null;
        };
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map;
        Student s;
        Teacher t;
        if (slList != null && !slList.isEmpty()) {
            for (StudentLeave sl : slList) {
                int submitState = sl.getState() != null ? sl.getState() : 0;
                if (!studentRole && submitState == 0) {
                    continue;
                }
                LeaveAuditView auditView = computeLeaveAuditView(sl);

                if (filterState != -1 && !matchesAuditFilter(filterState, submitState, auditView, studentRole)) {
                    continue;
                }

                map = new HashMap<>();
                s = sl.getStudent();
                t = sl.getTeacher();
                map.put("studentLeaveId", sl.getStudentLeaveId());
                map.put("studentNum", s.getPerson().getNum());
                map.put("studentName", s.getPerson().getName());
                map.put("studentId", s.getPersonId());
                map.put("teacherName", formatTeacherDisplayName(t));
                map.put("submitState", submitState);
                map.put("state", auditView.auditState());
                map.put("reason", sl.getReason());
                map.put("leaveDate", sl.getLeaveDate());
                map.put("returnDate", sl.getReturnDate());
                map.put("applyTime", formatDateTime(sl.getApplyTime()));
                map.put("teacherTime", formatDateTime(sl.getTeacherTime()));
                map.put("adminTime", formatDateTime(sl.getAdminTime()));
                map.put("lastActivityTime", computeLastActivityMillis(sl));
                map.put("adminComment", sl.getAdminComment());
                map.put("adminChecked", sl.getAdminChecked());
                map.put("adminPass", sl.getAdminPass());
                map.put("teacherId", t != null ? t.getPersonId() : null);
                map.put("teacherComment", sl.getTeacherComment());
                map.put("teacherChecked", sl.getTeacherChecked());
                map.put("teacherPass", sl.getTeacherPass());
                map.put("stateName", auditView.stateName());
                dataList.add(map);
            }
        }
        dataList.sort((a, b) -> Long.compare(
                ((Number) b.getOrDefault("lastActivityTime", 0L)).longValue(),
                ((Number) a.getOrDefault("lastActivityTime", 0L)).longValue()));
        for (Map<String, Object> item : dataList) {
            item.remove("lastActivityTime");
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

        if (reason != null && reason.length() > REASON_MAX_LENGTH) {
            return CommonMethod.getReturnMessageError("请假理由不能超过100字！");
        }

        boolean isSubmit = state != null && state == 1;
        if (isSubmit) {
            if (teacherId == null || teacherId <= 0) {
                return CommonMethod.getReturnMessageError("请选择指导老师！");
            }
            if (reason == null || reason.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("请假理由不能为空！");
            }
        }

        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isEmpty()) {
                return CommonMethod.getReturnMessageError("请假记录不存在！");
            }
            sl = op.get();
            if(!isOwnedByCurrentStudent(sl)) {
                return CommonMethod.getReturnMessageError("无权操作该请假单！");
            }
            if(Boolean.TRUE.equals(sl.getTeacherChecked()) || Boolean.TRUE.equals(sl.getAdminChecked())) {
                return CommonMethod.getReturnMessageError("该请假申请已审核，无法修改！");
            }
            if(sl.getState() != null && sl.getState() == 1 && state != null && state == 0) {
                return CommonMethod.getReturnMessageError("已提交的申请不能改回草稿！");
            }
        }
        if(sl == null) {
            Optional<Student> studentOp = studentRepository.findByPersonNum(CommonMethod.getUsername());
            if(studentOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("当前账号未关联学生档案，无法提交请假！");
            }
            sl = new StudentLeave();
            sl.setState(0);
            sl.setApplyTime(new Date());
            sl.setTeacherComment("");
            sl.setAdminComment("");
            sl.setStudent(studentOp.get());
            sl.setTeacherChecked(false);
            sl.setTeacherPass(false);
            sl.setAdminChecked(false);
            sl.setAdminPass(false);
        }
        if(teacherId != null && teacherId > 0) {
            Optional<Teacher> op = teacherRepository.findById(teacherId);
            if(op.isPresent()) {
                sl.setTeacher(op.get());
            } else if (isSubmit) {
                return CommonMethod.getReturnMessageError("所选指导老师不存在！");
            }
        }
        sl.setLeaveDate(leaveDate);
        sl.setReturnDate(returnDate);
        sl.setReason(reason);
        sl.setState(state);
        sl.setApplyTime(new Date());
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse studentLeaveDelete(DataRequest dataRequest) {
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        if(studentLeaveId == null || studentLeaveId <= 0) {
            return CommonMethod.getReturnMessageError("请选择要删除的请假记录！");
        }
        Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
        if(op.isEmpty()) {
            return CommonMethod.getReturnMessageError("请假记录不存在！");
        }
        StudentLeave sl = op.get();
        if(!isOwnedByCurrentStudent(sl)) {
            return CommonMethod.getReturnMessageError("无权操作该请假单！");
        }
        if(Boolean.TRUE.equals(sl.getTeacherChecked()) || Boolean.TRUE.equals(sl.getAdminChecked())) {
            return CommonMethod.getReturnMessageError("该请假申请已审核，无法删除！");
        }
        studentLeaveRepository.delete(sl);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse studentLeaveCheck(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        String userName = CommonMethod.getUsername();
        Integer state = dataRequest.getInteger("state");
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        String teacherComment = dataRequest.getString("teacherComment");
        String adminComment = dataRequest.getString("adminComment");
        if(studentLeaveId == null || studentLeaveId <= 0) {
            return CommonMethod.getReturnMessageError("请选择要审核的请假记录！");
        }
        Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
        if(op.isEmpty()) {
            return CommonMethod.getReturnMessageError("请假记录不存在！");
        }
        StudentLeave sl = op.get();
        if(!isSubmitted(sl)) {
            return CommonMethod.getReturnMessageError("该请假单尚未提交，无法审核！");
        }
        if(isAuditRejected(sl)) {
            return CommonMethod.getReturnMessageError("该请假单已审核不通过，无需再审！");
        }
        if("ROLE_ADMIN".equals(roleName)) {
            if(Boolean.TRUE.equals(sl.getAdminChecked())) {
                return CommonMethod.getReturnMessageError("该请假单管理员已审核，不可重复操作！");
            }
            if(!Boolean.TRUE.equals(sl.getTeacherChecked())) {
                return CommonMethod.getReturnMessageError("请等待教师审核后再操作！");
            }
            if(!Boolean.TRUE.equals(sl.getTeacherPass())) {
                return CommonMethod.getReturnMessageError("该请假单教师已审核不通过，无需再审！");
            }
            sl.setAdminComment(adminComment);
            sl.setAdminTime(new Date());
            sl.setAdminChecked(true);
            sl.setAdminPass(state != null && state == 0);
        } else if("ROLE_TEACHER".equals(roleName)) {
            if(!isAssignedTeacher(sl, userName)) {
                return CommonMethod.getReturnMessageError("无权审核该请假单！");
            }
            if(Boolean.TRUE.equals(sl.getTeacherChecked())) {
                return CommonMethod.getReturnMessageError("该请假单教师已审核，不可重复操作！");
            }
            sl.setTeacherComment(teacherComment);
            sl.setTeacherTime(new Date());
            sl.setTeacherChecked(true);
            sl.setTeacherPass(state != null && state == 0);
        } else {
            return CommonMethod.getReturnMessageError("无权审核该请假单！");
        }
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }

    /**
     * 判断请假记录是否属于当前登录学生。
     */
    private boolean isOwnedByCurrentStudent(StudentLeave sl) {
        if (sl == null || sl.getStudent() == null || sl.getStudent().getPerson() == null) {
            return false;
        }
        String ownerNum = sl.getStudent().getPerson().getNum();
        String currentNum = CommonMethod.getUsername();
        return ownerNum != null && ownerNum.equals(currentNum);
    }

    /**
     * 判断当前登录用户是否为该请假单的指派指导教师。
     */
    private boolean isAssignedTeacher(StudentLeave sl, String teacherNum) {
        if (sl == null || sl.getTeacher() == null || sl.getTeacher().getPerson() == null) {
            return false;
        }
        String assignedNum = sl.getTeacher().getPerson().getNum();
        return assignedNum != null && assignedNum.equals(teacherNum);
    }

    private boolean isSubmitted(StudentLeave sl) {
        return sl.getState() != null && sl.getState() == 1;
    }

    private boolean isAuditRejected(StudentLeave sl) {
        if (Boolean.TRUE.equals(sl.getTeacherChecked()) && !Boolean.TRUE.equals(sl.getTeacherPass())) {
            return true;
        }
        return Boolean.TRUE.equals(sl.getAdminChecked()) && !Boolean.TRUE.equals(sl.getAdminPass());
    }

    private record LeaveAuditView(int auditState, String stateName) {}

    private LeaveAuditView computeLeaveAuditView(StudentLeave sl) {
        int submitState = sl.getState() != null ? sl.getState() : 0;
        boolean teacherChecked = Boolean.TRUE.equals(sl.getTeacherChecked());
        boolean adminChecked = Boolean.TRUE.equals(sl.getAdminChecked());
        boolean teacherPass = Boolean.TRUE.equals(sl.getTeacherPass());
        boolean adminPass = Boolean.TRUE.equals(sl.getAdminPass());

        if (submitState == 0 && !teacherChecked && !adminChecked) {
            return new LeaveAuditView(0, "草稿");
        }
        if (teacherChecked && !teacherPass) {
            return new LeaveAuditView(3, "审核不通过");
        }
        if (adminChecked && !adminPass) {
            return new LeaveAuditView(3, "审核不通过");
        }
        if (teacherChecked && adminChecked && teacherPass && adminPass) {
            return new LeaveAuditView(2, "审核通过");
        }
        if (teacherChecked || adminChecked) {
            return new LeaveAuditView(1, "审核中");
        }
        return new LeaveAuditView(0, "待审核");
    }

    private boolean matchesAuditFilter(int filterState, int submitState, LeaveAuditView auditView, boolean studentRole) {
        if (filterState == FILTER_STATE_DRAFT) {
            return studentRole && submitState == 0 && "草稿".equals(auditView.stateName());
        }
        if (filterState == 0) {
            return submitState == 1 && "待审核".equals(auditView.stateName());
        }
        return filterState == auditView.auditState();
    }

    private static long computeLastActivityMillis(StudentLeave sl) {
        long last = 0L;
        last = Math.max(last, toMillis(sl.getApplyTime()));
        last = Math.max(last, toMillis(sl.getTeacherTime()));
        last = Math.max(last, toMillis(sl.getAdminTime()));
        return last;
    }

    private static long toMillis(Date date) {
        return date != null ? date.getTime() : 0L;
    }

    private static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 教师在下拉与列表中的展示名：优先姓名，否则用工号；教师或人员为空时返回空串。
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
