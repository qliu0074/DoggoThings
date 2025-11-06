package app.nail.domain.repository;

import app.nail.domain.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 商品图片仓储
 * 作用：按商品读取图片，封面优先
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /** 按商品查所有图片，按 sort_order 升序 */
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    /** 查询某商品是否已有封面 */
    boolean existsByProductIdAndCoverTrue(Long productId);
}
