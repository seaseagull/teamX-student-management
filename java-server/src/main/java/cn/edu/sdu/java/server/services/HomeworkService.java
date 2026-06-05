package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.management.PlatformLoggingMXBean;
import java.util.*;

@AllArgsConstructor
@Service
public class HomeworkService {
    private final StudentRepository studentRepository;
    private final HomeworkRepository homeworkRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;

    public OptionItemList getCourseItemOptionList(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        List<StudentCourse> scList = studentCourseRepository.findByStudentPersonId(studentId);  //数据库查询操作
        List<OptionItem> itemList = new ArrayList<>();
        for (StudentCourse sc : scList) {
            Course c = sc.getCourse();
            itemList.add(new OptionItem(c.getCourseId(), c.getCourseId()+"", c.getNum() + "-" + c.getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getHomeworkList(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        String search = dataRequest.getString("search");
        if(search == null) search = "";
        assert roleName != null;
        Integer state = dataRequest.getInteger("state");
        if (state == null) state = -1;
        Integer courseId = dataRequest.getInteger("courseId");
        if (courseId == null) courseId = -1;
        Integer studentId = CommonMethod.getPersonId();
        List<Homework> hList = switch (roleName) {
            case "ROLE_STUDENT" -> homeworkRepository.getHomeworkList(studentId, search, state, courseId);
            case "ROLE_TEACHER" -> homeworkRepository.getHomeworkList(studentId, search, state, courseId);
            case "ROLE_ADMIN" -> homeworkRepository.getHomeworkList(studentId, search, state, courseId);
            default -> null;
        };
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map;
        if (hList != null && !hList.isEmpty()) {
            for (Homework h : hList) {
                map = new HashMap<>();
                map.put("homeworkId", h.getHomeworkId());
                map.put("homeworkName", h.getName());
                map.put("courseId", String.valueOf(h.getCourse().getCourseId()));
                map.put("courseName", h.getCourse().getNum() + h.getCourse().getName());
                map.put("deadline", h.getDeadline());
                map.put("stateName", h.getState() != null && h.getState() == 1 ? "已完成" : "未完成");
                map.put("remark", h.getRemark());
                map.put("state", String.valueOf(h.getState()));
                map.put("teacherName", h.getCourse().getTeacher() != null ?
                        h.getCourse().getTeacher().getPerson().getName() : "");
                dataList.add(map);
            }
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse homeworkSave(DataRequest dataRequest) {
        Integer state = dataRequest.getInteger("state");
        Integer homeworkId = dataRequest.getInteger("homeworkId");
        Integer courseId = dataRequest.getInteger("courseId");
        String name = dataRequest.getString("homeworkName");
        String deadline = dataRequest.getString("deadline");
        String remark = dataRequest.getString("remark");
        Homework h = null;
        if(homeworkId != null && homeworkId > 0) {
            Optional<Homework> op = homeworkRepository.findById(homeworkId);
            if(op.isPresent())
                h = op.get();
        }
        if(h == null) {
            h = new Homework();
            h.setStudent(studentRepository.findByPersonNum(CommonMethod.getUsername()).get());
        }
        if(courseId != null && courseId > 0){
            Optional<Course> op = courseRepository.findById(courseId);
            if (op.isPresent()) {
                Course course = op.get();
                h.setCourse(course);
            }
        }
        h.setName(name);
        h.setDeadline(deadline);
        h.setRemark(remark);
        h.setState(state != null ? state : 0);
        homeworkRepository.save(h);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse homeworkDelete(DataRequest dataRequest) {
        Integer homeworkId = dataRequest.getInteger("homeworkId");

        if(homeworkId != null&& homeworkId > 0){
            homeworkRepository.deleteById(homeworkId);
        }

        return CommonMethod.getReturnMessageOK();
    }
}
