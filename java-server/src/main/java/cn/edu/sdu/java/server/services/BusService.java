package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BusSchedule;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BusScheduleRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusService {

    private final BusScheduleRepository busScheduleRepository;

    public BusService(BusScheduleRepository busScheduleRepository) {
        this.busScheduleRepository = busScheduleRepository;
    }

    public DataResponse getBusSchedule(DataRequest dataRequest) {
        String fromCampus = dataRequest.getString("fromCampus");
        String toCampus = dataRequest.getString("toCampus");
        String scheduleType = dataRequest.getString("scheduleType");

        if (fromCampus == null || toCampus == null || scheduleType == null) {
            return CommonMethod.getReturnMessageError("参数不完整");
        }

        List<BusSchedule> scheduleList = busScheduleRepository.findByFromCampusAndToCampusAndScheduleTypeOrderByDepartureTimeAsc(
                fromCampus, toCampus, scheduleType);

        List<String> timeList = new ArrayList<>();
        for (BusSchedule schedule : scheduleList) {
            timeList.add(schedule.getDepartureTime());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("timeList", timeList);
        result.put("fromCampus", fromCampus);
        result.put("toCampus", toCampus);

        return CommonMethod.getReturnData(result);
    }
}