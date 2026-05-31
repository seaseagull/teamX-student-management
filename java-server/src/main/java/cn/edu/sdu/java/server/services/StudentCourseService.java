package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Homework;
import cn.edu.sdu.java.server.models.StudentCourse;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.StudentCourseRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class StudentCourseService {
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public OptionItemList getTeacherItemOptionList(DataRequest dataRequest) {
        List<Teacher> tList = teacherRepository.findAll();
        List<OptionItem> itemList = new ArrayList<>();
        for(Teacher t : tList) {
            itemList.add(new OptionItem(t.getPersonId(),t.getPersonId()+"",t.getPerson().getName()));
        }
        return new OptionItemList(0,itemList);
    }

    public DataResponse getCourseList(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        String search = dataRequest.getString("search");
        if(search == null) search = "";
        assert roleName != null;
        Integer state = dataRequest.getInteger("state");
        if (state == null) state = -1;
        Integer teacherId = dataRequest.getInteger("teacherId");
        if (teacherId == null) teacherId = -1;
        Integer credit = dataRequest.getInteger("credit");
        if (credit == null) credit = -1;
        Integer studentId = CommonMethod.getPersonId();

        List<Course> cList = courseRepository.getCourseList(search,credit,teacherId);

        List<StudentCourse> scList = studentCourseRepository.findByStudentPersonId(studentId);
        Set<Integer> selectedCourseIds = new HashSet<>();
        for (StudentCourse sc : scList) {
            selectedCourseIds.add(sc.getCourse().getCourseId());
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map;
        if (cList != null && !cList.isEmpty()) {
            for (Course c : cList) {
                boolean isSelected = selectedCourseIds.contains(c.getCourseId());

                // 按已选/未选筛选
                if (state == 0 && !isSelected) continue;  // 只查已选，但没选 → 跳过
                if (state == 1 && isSelected) continue;   // 只查未选，但选了 → 跳过
                map = new HashMap<>();
                map.put("courseId", c.getCourseId());
                map.put("num", c.getNum());
                map.put("name", c.getName());
                map.put("teacherId", String.valueOf(c.getTeacher().getPersonId()));
                map.put("teacherName", c.getTeacher().getPerson().getName());
                map.put("credit", c.getCredit());
                map.put("preCourse", c.getPreCourse() != null ? c.getPreCourse().getName() : "");                map.put("isSelected", isSelected);
                dataList.add(map);
            }
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse courseSelect(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        Integer courseId = dataRequest.getInteger("courseId");

        StudentCourse sc = new StudentCourse();
        sc.setStudent(studentRepository.findById(studentId).get());
        sc.setCourse(courseRepository.findById(courseId).get());
        studentCourseRepository.save(sc);

        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse courseCancel(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        Integer courseId = dataRequest.getInteger("courseId");

        studentCourseRepository.deleteByStudentPersonIdAndCourseCourseId(studentId,courseId);

        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse getStudentCourses(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();

        List<StudentCourse> scourses = studentCourseRepository.findByStudentPersonId(studentId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (StudentCourse sc : scourses) {
            Map<String, Object> map = new HashMap<>();
            map.put("courseId", sc.getCourse().getCourseId());
            map.put("num", sc.getCourse().getNum());
            map.put("name", sc.getCourse().getName());
            map.put("credit", sc.getCourse().getCredit());
            map.put("teacherName", sc.getCourse().getTeacher().getPerson().getName());
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }
}
