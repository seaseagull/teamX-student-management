package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.CalendarEvent;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CalendarEventRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class CalendarService {

    private final CalendarEventRepository repo;

    public CalendarService(CalendarEventRepository repo) {
        this.repo = repo;
    }

    public DataResponse getMonthEvents(DataRequest req) {
        int year = req.getInteger("year");
        int month = req.getInteger("month");

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<CalendarEvent> list = repo.findByDateBetween(start, end);
        List<Map<String, Object>> res = new ArrayList<>();

        for (CalendarEvent e : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getTitle());
            map.put("date", e.getDate().toString());
            map.put("type", e.getType());
            map.put("color", e.getColor());
            res.add(map);
        }
        return CommonMethod.getReturnData(res);
    }
}