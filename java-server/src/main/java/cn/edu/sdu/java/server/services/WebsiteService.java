package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Website;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.WebsiteRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class WebsiteService {
    private final  WebsiteRepository websiteRepository;

    public DataResponse getWebsites(DataRequest dataRequest) {
        List<Website> websites = websiteRepository.findAllByOrderBySortOrderAsc();
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Website w : websites) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", w.getId());
            map.put("name", w.getName());
            map.put("url", w.getUrl());
            map.put("description",w.getDescription());
            map.put("category", w.getCategory());
            dataList.add(map);
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse saveWebsite(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");

        Website website;
        if (id != null) {
            website = websiteRepository.findById(id).orElse(new Website());
        } else {
            website = new Website();
            Integer maxSort = websiteRepository.findMaxSortOrder();
            if (maxSort == null) maxSort = 0;
            website.setSortOrder(maxSort + 1);
        }
        String name = dataRequest.getString("name");
        String url = dataRequest.getString("url");
        String description = dataRequest.getString("description");
        String category = dataRequest.getString("category");

        website.setName(name);
        website.setUrl(url);
        website.setDescription(description);
        website.setCategory(category);


        websiteRepository.save(website);
        return CommonMethod.getReturnMessageOK();

    }

    public DataResponse deleteWebsite(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        if (id != null) {
            websiteRepository.deleteById(id);
        }
        return CommonMethod.getReturnMessageOK();
    }
}
