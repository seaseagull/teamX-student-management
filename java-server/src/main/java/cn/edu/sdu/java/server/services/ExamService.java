package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Exam;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.ExamRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExamService {
    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Map<String, Object> getMapFromExam(Exam e) {
        Map<String, Object> m = new HashMap<>();
        if (e == null) return m;
        m.put("examId", e.getExamId());
        m.put("examName", e.getExamName());
        m.put("examType", e.getExamType());
        m.put("startTime", e.getStartTime() != null ? e.getStartTime().format(FORMATTER) : null);
        m.put("endTime", e.getEndTime() != null ? e.getEndTime().format(FORMATTER) : null);
        m.put("duration", e.getDuration());
        m.put("location", e.getLocation());
        m.put("status", e.getStatus());
        m.put("createTime", e.getCreateTime() != null ? e.getCreateTime().format(FORMATTER) : null);
        m.put("updateTime", e.getUpdateTime() != null ? e.getUpdateTime().format(FORMATTER) : null);
        return m;
    }

    public DataResponse getExamList(DataRequest dataRequest) {
        String examName = dataRequest.getString("examName");
        Integer currentPage = dataRequest.getCurrentPage();
        Integer pageSize = dataRequest.getInteger("pageSize");
        if (currentPage == null) currentPage = 0;
        if (pageSize == null) pageSize = 40;

        List<Map<String, Object>> dataList = new ArrayList<>();
        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Exam> page = examRepository.findExamPageByName(examName == null ? "" : examName, pageable);

        Map<String, Object> data = new HashMap<>();
        if (page != null) {
            data.put("dataTotal", page.getTotalElements());
            data.put("pageSize", pageSize);
            for (Exam exam : page.getContent()) {
                dataList.add(getMapFromExam(exam));
            }
        }
        data.put("dataList", dataList);
        return CommonMethod.getReturnData(data);
    }

    public DataResponse getExamInfo(DataRequest dataRequest) {
        Long examId = dataRequest.getLong("examId");
        Exam e = null;
        if (examId != null) {
            Optional<Exam> op = examRepository.findById(examId);
            if (op.isPresent()) e = op.get();
        }
        return CommonMethod.getReturnData(getMapFromExam(e));
    }

    public DataResponse examEditSave(DataRequest dataRequest) {
        Map<String, Object> form = dataRequest.getMap("form");
        Long examId = CommonMethod.getLong(form, "examId");
        Exam e;
        boolean isNew = false;

        if (examId != null) {
            Optional<Exam> op = examRepository.findById(examId);
            e = op.orElseGet(() -> {
                Exam newExam = new Exam();
                newExam.setExamId(examId);
                return newExam;
            });
        } else {
            e = new Exam();
            isNew = true;
        }

        // ✅ 只改这2行，其他所有代码完全不动
        e.setExamName(form.get("examName") == null ? "" : form.get("examName").toString().trim());
        e.setExamType(form.get("examType") == null ? "期中考试" : form.get("examType").toString().trim());

        String startTimeStr = CommonMethod.getString(form, "startTime");
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            e.setStartTime(java.time.LocalDateTime.parse(startTimeStr, FORMATTER));
        }

        String endTimeStr = CommonMethod.getString(form, "endTime");
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            e.setEndTime(java.time.LocalDateTime.parse(endTimeStr, FORMATTER));
        }

        e.setDuration(CommonMethod.getInteger(form, "duration"));
        e.setLocation(CommonMethod.getString(form, "location"));
        e.setStatus(CommonMethod.getString(form, "status"));
        if (isNew) {
            e.setCreateTime(java.time.LocalDateTime.now());
        }
        e.setUpdateTime(java.time.LocalDateTime.now());
        examRepository.save(e);
        return CommonMethod.getReturnData(e.getExamId());
    }

    public DataResponse examDelete(DataRequest dataRequest) {
        Long examId = dataRequest.getLong("examId");
        if (examId != null && examRepository.existsById(examId)) {
            examRepository.deleteById(examId);
        }
        return CommonMethod.getReturnMessageOK();
    }

}