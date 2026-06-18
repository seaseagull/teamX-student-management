package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.NoticeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 获取通知列表（所有角色可看）
    @PostMapping("/getList")
    public DataResponse getNoticeList(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.getNoticeList(dataRequest);
    }

    // 保存通知（仅管理员）
    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse saveNotice(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.saveNotice(dataRequest);
    }

    // 删除通知（仅管理员）
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse deleteNotice(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.deleteNotice(dataRequest);
    }
}