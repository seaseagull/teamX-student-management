package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@AllArgsConstructor
@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public OptionItemList getPreCourseItemOptionList(DataRequest dataRequest) {
        List<Course> cList = courseRepository.findAll();
        List<OptionItem> itemList = new ArrayList<>();
        itemList.add(new OptionItem(0, "0", "无"));
        for (Course c : cList) {
            itemList.add(new OptionItem(
                    c.getCourseId(),
                    c.getCourseId() + "",
                    c.getNum() + "-" + c.getName()
            ));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getCourseList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        if(numName == null)
            numName = "";
        List<Course> cList = courseRepository.findCourseListByNumName(numName);  //数据库查询操作
        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String,Object> m;
        Course pc;
        for (Course c : cList) {
            m = new HashMap<>();
            m.put("courseId", c.getCourseId()+"");
            m.put("num",c.getNum());
            m.put("name",c.getName());
            m.put("credit",c.getCredit()+"");
            m.put("coursePath",c.getCoursePath());
            pc =c.getPreCourse();
            if(pc != null) {
                m.put("preCourse",pc.getName());
                m.put("preCourseId",pc.getCourseId());
            }
            if (c.getTeacher() != null) {
                m.put("teacherId", c.getTeacher().getPersonId());
                m.put("teacherName", c.getTeacher().getPerson().getNum() + "-"
                        + c.getTeacher().getPerson().getName());
            } else {
                m.put("teacherId", "");
                m.put("teacherName", "");
            }
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse courseSave(DataRequest dataRequest) {
        Integer courseId = dataRequest.getInteger("courseId");
        String num = dataRequest.getString("num");
        String name = dataRequest.getString("name");
        String coursePath = dataRequest.getString("coursePath");
        Integer credit = dataRequest.getInteger("credit");
        Integer teacherId = dataRequest.getInteger("teacherId");
        Integer preCourseId = dataRequest.getInteger("preCourseId");
        Optional<Course> op;
        Course c= null;

        if(courseId != null) {
            op = courseRepository.findById(courseId);
            if(op.isPresent())
                c= op.get();
        }
        if(c== null)
            c = new Course();
        Course pc =null;
        if(preCourseId != null) {
            op = courseRepository.findById(preCourseId);
            if(op.isPresent())
                pc = op.get();
        }
        c.setNum(num);
        c.setName(name);
        c.setCredit(credit);
        c.setCoursePath(coursePath);
        c.setPreCourse(pc);
        if (teacherId != null && teacherId > 0) {
            teacherRepository.findById(teacherId).ifPresent(c::setTeacher);
        }

        courseRepository.save(c);
        return CommonMethod.getReturnMessageOK();
    }
    public DataResponse courseDelete(DataRequest dataRequest) {
        Integer courseId = dataRequest.getInteger("courseId");
        Optional<Course> op;
        Course c= null;
        if(courseId != null) {
            op = courseRepository.findById(courseId);
            if(op.isPresent()) {
                c = op.get();
                courseRepository.delete(c);
            }
        }
        return CommonMethod.getReturnMessageOK();
    }

}
