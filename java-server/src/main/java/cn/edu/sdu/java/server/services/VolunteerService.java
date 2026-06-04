package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.VolunteerActivity;
import cn.edu.sdu.java.server.models.VolunteerSignup;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.VolunteerActivityRepository;
import cn.edu.sdu.java.server.repositorys.VolunteerSignupRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.AllArgsConstructor;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class VolunteerService {
    private final VolunteerActivityRepository activityRepository;
    private final VolunteerSignupRepository signupRepository;
    private final StudentRepository studentRepository;

    public DataResponse getActivityList(DataRequest dataRequest) {
        List<VolunteerActivity> activities = activityRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (VolunteerActivity a : activities) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("location", a.getLocation());
            map.put("activityDate", a.getActivityDate() != null ? a.getActivityDate().toString() : "");
            map.put("startTime", a.getStartTime() != null ? a.getStartTime().toString() : "");
            map.put("endTime", a.getEndTime() != null ? a.getEndTime().toString() : "");
            map.put("workDescription", a.getWorkDescription());
            map.put("recruitCount", a.getRecruitCount());
            map.put("volunteerHours", a.getVolunteerHours());
            map.put("requirements", a.getRequirements());
            map.put("notes", a.getNotes());
            map.put("signupStart", a.getSignupStart() != null ?
                    a.getSignupStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            map.put("signupEnd", a.getSignupEnd() != null ?
                    a.getSignupEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            map.put("status", a.getStatus());
            map.put("signedCount", signupRepository.countByActivityId(a.getId()));
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse activitySave(DataRequest dataRequest) {
        Integer activityId = dataRequest.getInteger("activityId");

        VolunteerActivity a;
        if (activityId != null) {
            a = activityRepository.findById(activityId).orElse(new VolunteerActivity());
        } else {
            a = new VolunteerActivity();
        }
        a.setName(dataRequest.getString("name"));
        a.setLocation(dataRequest.getString("location"));
        String dateStr = dataRequest.getString("activityDate");
        a.setActivityDate(LocalDate.parse(dateStr));
        String startStr = dataRequest.getString("startTime");
        a.setStartTime(LocalTime.parse(startStr));
        String endStr = dataRequest.getString("endTime");
        a.setEndTime(LocalTime.parse(endStr));
        a.setWorkDescription(dataRequest.getString("workDescription"));
        a.setRecruitCount(dataRequest.getInteger("recruitCount"));
        String hoursStr = dataRequest.getString("volunteerHours");
        a.setVolunteerHours(new BigDecimal(hoursStr));
        a.setRequirements(dataRequest.getString("requirements"));
        a.setNotes(dataRequest.getString("notes"));
        String signupStartStr = dataRequest.getString("signupStart");
        a.setSignupStart(LocalDateTime.parse(signupStartStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        String signupEndStr = dataRequest.getString("signupEnd");
        a.setSignupEnd(LocalDateTime.parse(signupEndStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        a.setStatus("PENDING");
        activityRepository.save(a);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse changeStatus(DataRequest dataRequest) {
        Integer activityId = dataRequest.getInteger("activityId");
        String newStatus = dataRequest.getString("status");

        VolunteerActivity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) return CommonMethod.getReturnMessage(1, "活动不存在");

        activity.setStatus(newStatus);

        if ("FINISHED".equals(newStatus)) {
            List<VolunteerSignup> signups = signupRepository.findByActivityId(activityId);
            for (VolunteerSignup s : signups) {
                if ("SIGNED".equals(s.getStatus())) {
                    s.setStatus("COMPLETED");
                    s.setHoursEarned(activity.getVolunteerHours());
                    signupRepository.save(s);
                }
            }
        }

        activityRepository.save(activity);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse signup(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        Integer activityId = dataRequest.getInteger("activityId");

        if (signupRepository.existsByActivityIdAndStudentPersonId(activityId, studentId)) {
            return CommonMethod.getReturnMessage(1, "已报名过该活动");
        }

        VolunteerActivity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) return CommonMethod.getReturnData(1, "活动不存在");
        long signedCount = signupRepository.countByActivityId(activityId);
        if (signedCount >= activity.getRecruitCount()) {
            return CommonMethod.getReturnMessage(1,"报名人数已满");
        }

        VolunteerSignup signup = new VolunteerSignup();
        signup.setActivity(activity);
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) return CommonMethod.getReturnMessage(1, "学生不存在");
        signup.setStudent(student);
        signup.setStatus("SIGNED");

        signupRepository.save(signup);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse cancelSignup(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        Integer activityId = dataRequest.getInteger("activityId");

        signupRepository.cancelSignup(activityId, studentId);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse checkSignup(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        Integer activityId = dataRequest.getInteger("activityId");

        boolean exists = signupRepository.existsByActivityIdAndStudentPersonId(activityId, studentId);
        return exists ? CommonMethod.getReturnMessage(1, "已报名") : CommonMethod.getReturnMessage(0, "未报名");
    }

    public DataResponse getStudentActivityList(DataRequest dataRequest) {
        Integer studentId = CommonMethod.getPersonId();
        List<VolunteerSignup> signups = signupRepository.findByStudentPersonIdOrderBySignupTimeDesc(studentId);

        double totalHours = 0;
        int count = 0;
        List<Map<String, Object>> activities = new ArrayList<>();

        for (VolunteerSignup s : signups) {
            VolunteerActivity a = s.getActivity();
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("location", a.getLocation());
            map.put("activityDate", a.getActivityDate() != null ? a.getActivityDate().toString() : "");
            // 补全字段
            map.put("startTime", a.getStartTime() != null ? a.getStartTime().toString() : "");
            map.put("endTime", a.getEndTime() != null ? a.getEndTime().toString() : "");
            map.put("workDescription", a.getWorkDescription());
            map.put("volunteerHours", a.getVolunteerHours());
            map.put("requirements", a.getRequirements());
            map.put("notes", a.getNotes());
            map.put("signupStart", a.getSignupStart() != null ?
                    a.getSignupStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            map.put("signupEnd", a.getSignupEnd() != null ?
                    a.getSignupEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");

            map.put("hoursEarned", s.getHoursEarned());
            map.put("signupStatus", s.getStatus());
            map.put("status", a.getStatus());
            map.put("recruitCount", a.getRecruitCount());
            map.put("signedCount", signupRepository.countByActivityId(a.getId()));

            if ("COMPLETED".equals(s.getStatus())) {
                totalHours += s.getHoursEarned() != null ? s.getHoursEarned().doubleValue() : 0;
                count++;
            }
            activities.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalHours", totalHours);
        result.put("activityCount", count);
        result.put("activities", activities);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getVolunteers(DataRequest dataRequest) {
        Integer activityId = dataRequest.getInteger("activityId");
        List<VolunteerSignup> signups = signupRepository.findByActivityId(activityId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (VolunteerSignup s : signups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            Person p = s.getStudent().getPerson();
            map.put("name", p.getName());
            map.put("num", p.getNum());
            map.put("gender", p.getGender() != null ? p.getGender() : "");
            map.put("phone", p.getPhone() != null ? p.getPhone() : "");
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse volunteerDelete(DataRequest dataRequest) {
        Integer signupId = dataRequest.getInteger("signupId");
        signupRepository.deleteById(signupId);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse deleteActivity(DataRequest dataRequest) {
        Integer activityId = dataRequest.getInteger("activityId");
        if (activityId != null) {
            activityRepository.deleteById(activityId);
        }
        return CommonMethod.getReturnMessageOK();
    }
}
