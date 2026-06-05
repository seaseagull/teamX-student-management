package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Dormitory;
import cn.edu.sdu.java.server.models.StudentDorm;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.DormitoryRepository;
import cn.edu.sdu.java.server.repositorys.StudentDormRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DormitoryService {

    private final DormitoryRepository dormitoryRepository;
    private final StudentDormRepository studentDormRepository;

    // 构造器注入（和你所有Service完全一致）
    public DormitoryService(DormitoryRepository dormitoryRepository, StudentDormRepository studentDormRepository) {
        this.dormitoryRepository = dormitoryRepository;
        this.studentDormRepository = studentDormRepository;
    }

    // 获取所有正常宿舍列表
    public DataResponse getDormitoryList(DataRequest dataRequest) {
        List<Dormitory> dormList = dormitoryRepository.findByStatus(1);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> m;
        for (Dormitory dorm : dormList) {
            m = new HashMap<>();
            m.put("id", dorm.getId() + "");
            m.put("buildingNo", dorm.getBuildingNo());
            m.put("roomNo", dorm.getRoomNo());
            m.put("bedCount", dorm.getBedCount() + "");
            m.put("usedBed", dorm.getUsedBed() + "");
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    // 新增宿舍
    @Transactional
    public DataResponse dormitorySave(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        String buildingNo = dataRequest.getString("buildingNo");
        String roomNo = dataRequest.getString("roomNo");
        Integer bedCount = dataRequest.getInteger("bedCount");

        Optional<Dormitory> op;
        Dormitory dorm = null;
        if (id != null) {
            op = dormitoryRepository.findById(id);
            if (op.isPresent()) {
                dorm = op.get();
            }
        }
        if (dorm == null) {
            dorm = new Dormitory();
            dorm.setCreateTime(LocalDateTime.now());
            dorm.setUsedBed(0);
            dorm.setStatus(1);
        }
        dorm.setBuildingNo(buildingNo);
        dorm.setRoomNo(roomNo);
        dorm.setBedCount(bedCount == null ? 4 : bedCount);
        dorm.setUpdateTime(LocalDateTime.now());

        dormitoryRepository.save(dorm);
        return CommonMethod.getReturnMessageOK();
    }

    // 删除宿舍（逻辑删除）
    @Transactional
    public DataResponse dormitoryDelete(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        Optional<Dormitory> op;
        if (id != null) {
            op = dormitoryRepository.findById(id);
            if (op.isPresent()) {
                Dormitory dorm = op.get();
                dorm.setStatus(0);
                dorm.setUpdateTime(LocalDateTime.now());
                dormitoryRepository.save(dorm);
            }
        }
        return CommonMethod.getReturnMessageOK();
    }

    // 分配学生到宿舍
    @Transactional
    public DataResponse assignStudent(DataRequest dataRequest) {
        Integer studentId = dataRequest.getInteger("studentId");
        Integer dormId = dataRequest.getInteger("dormId");
        String bedNo = dataRequest.getString("bedNo");

        Optional<Dormitory> dormOp = dormitoryRepository.findById(dormId);
        if (!dormOp.isPresent()) {
            return CommonMethod.getReturnMessageError("宿舍不存在");
        }
        Dormitory dorm = dormOp.get();
        if (dorm.getUsedBed() >= dorm.getBedCount()) {
            return CommonMethod.getReturnMessageError("宿舍床位已满");
        }

        // 更新宿舍已用床位
        dorm.setUsedBed(dorm.getUsedBed() + 1);
        dorm.setUpdateTime(LocalDateTime.now());
        dormitoryRepository.save(dorm);

        // 保存分配记录
        StudentDorm studentDorm = new StudentDorm();
        studentDorm.setStudentId(studentId);
        studentDorm.setDormId(dormId);
        studentDorm.setBedNo(bedNo);
        studentDorm.setCheckInDate(LocalDate.now());
        studentDorm.setStatus(1);
        studentDorm.setCreateTime(LocalDateTime.now());
        studentDormRepository.save(studentDorm);

        return CommonMethod.getReturnMessageOK();
    }

    // 学生退宿
    @Transactional
    public DataResponse checkOutStudent(DataRequest dataRequest) {
        Integer studentDormId = dataRequest.getInteger("studentDormId");
        Optional<StudentDorm> sdOp = studentDormRepository.findById(studentDormId);
        if (sdOp.isPresent()) {
            StudentDorm sd = sdOp.get();
            if (sd.getStatus() == 1) {
                // 标记退宿
                sd.setStatus(0);
                sd.setCheckOutDate(LocalDate.now());
                studentDormRepository.save(sd);

                // 减少宿舍已用床位
                Optional<Dormitory> dormOp = dormitoryRepository.findById(sd.getDormId());
                if (dormOp.isPresent()) {
                    Dormitory dorm = dormOp.get();
                    dorm.setUsedBed(Math.max(0, dorm.getUsedBed() - 1));
                    dorm.setUpdateTime(LocalDateTime.now());
                    dormitoryRepository.save(dorm);
                }
            }
        }
        return CommonMethod.getReturnMessageOK();
    }

    // 查询宿舍内的在住学生
    public DataResponse getStudentListInDorm(DataRequest dataRequest) {
        Integer dormId = dataRequest.getInteger("dormId");
        List<StudentDorm> studentList = studentDormRepository.findByDormIdAndStatus(dormId, 1);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> m;
        for (StudentDorm sd : studentList) {
            m = new HashMap<>();
            m.put("id", sd.getId() + "");
            m.put("studentId", sd.getStudentId() + "");
            m.put("bedNo", sd.getBedNo());
            m.put("checkInDate", sd.getCheckInDate().toString());
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }
}