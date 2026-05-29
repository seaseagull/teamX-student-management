package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Website;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebsiteRepository extends JpaRepository<Website, Integer> {
    List<Website> findAllByOrderBySortOrderAsc();
}
