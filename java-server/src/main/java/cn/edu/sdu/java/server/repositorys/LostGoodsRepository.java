package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.LostGoods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostGoodsRepository extends JpaRepository<LostGoods, Long> {}