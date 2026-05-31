package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Website;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WebsiteRepository extends JpaRepository<Website, Integer> {
    List<Website> findAllByOrderBySortOrderAsc();

    @Query("SELECT COALESCE(MAX(w.sortOrder), 0) FROM Website w")
    Integer findMaxSortOrder();
}
