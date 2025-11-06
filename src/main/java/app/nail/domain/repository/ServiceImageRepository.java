package app.nail.domain.repository;

import app.nail.domain.entity.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 服务图片仓储
 * 作用：按服务读取图片，封面优先
 */
public interface ServiceImageRepository extends JpaRepository<ServiceImage, Long> {

    List<ServiceImage> findByServiceIdOrderBySortOrderAsc(Long serviceId);

    boolean existsByServiceIdAndCoverTrue(Long serviceId);
}
