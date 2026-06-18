package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Notice;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.NoticeRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 获取通知列表
    public DataResponse getNoticeList(DataRequest dataRequest) {
        List<Notice> noticeList = noticeRepository.findAllByOrderByCreateTimeDesc();
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Notice notice : noticeList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("title", notice.getTitle());
            map.put("content", notice.getContent());
            map.put("createTime", notice.getCreateTime() != null ? notice.getCreateTime().format(formatter) : "");
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }

    // 保存通知（新增或修改）
    public DataResponse saveNotice(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        String title = dataRequest.getString("title");
        String content = dataRequest.getString("content");

        if (title == null || title.trim().isEmpty()) {
            return CommonMethod.getReturnMessageError("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return CommonMethod.getReturnMessageError("内容不能为空");
        }

        Notice notice;
        if (id != null && id > 0) {
            Optional<Notice> op = noticeRepository.findById(id);
            if (op.isPresent()) {
                notice = op.get();
            } else {
                notice = new Notice();
            }
        } else {
            notice = new Notice();
            notice.setCreateTime(LocalDateTime.now());
        }
        notice.setTitle(title);
        notice.setContent(content);
        notice.setUpdateTime(LocalDateTime.now());
        noticeRepository.save(notice);
        return CommonMethod.getReturnMessageOK();
    }

    // 删除通知
    public DataResponse deleteNotice(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        if (id != null && id > 0) {
            noticeRepository.deleteById(id);
        }
        return CommonMethod.getReturnMessageOK();
    }
}