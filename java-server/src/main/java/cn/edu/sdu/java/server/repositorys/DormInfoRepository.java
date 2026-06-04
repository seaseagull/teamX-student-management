package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.DormInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormInfoRepository extends JpaRepository<DormInfo, Long> {}