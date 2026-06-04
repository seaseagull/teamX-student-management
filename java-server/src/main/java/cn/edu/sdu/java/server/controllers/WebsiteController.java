package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.services.HomeworkService;
import cn.edu.sdu.java.server.services.WebsiteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/website")
public class WebsiteController {
    private final WebsiteService websiteService;

    @PostMapping("/getWebsites")
    public DataResponse getWebsites(@Valid @RequestBody DataRequest dataRequest) {
        return websiteService.getWebsites(dataRequest);
    }

    @PostMapping("/saveWebsite")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse saveWebsite(@Valid @RequestBody DataRequest dataRequest) {
        return websiteService.saveWebsite(dataRequest);
    }

    @PostMapping("/deleteWebsite")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse deleteWebsite(@Valid @RequestBody DataRequest dataRequest) {
        return websiteService.deleteWebsite(dataRequest);
    }
}