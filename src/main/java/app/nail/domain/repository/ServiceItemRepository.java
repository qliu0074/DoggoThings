package app.nail.domain.repository;

import app.nail.domain.entity.ServiceItem;
import app.nail.domain.enums.ProductStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 服务项目Repository
 * 处理美甲服务项的查询和管理
 */
@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
        /** 分类 + 状态 + 分页 */
    Page<ServiceItem> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);

    /** 仅按状态分页（用于 category 为空时） */
    Page<ServiceItem> findByStatus(ProductStatus status, Pageable pageable);

    
    /**
     * 查询指定状态的服务
     */
    List<ServiceItem> findByStatus(ProductStatus status);
    
    /**
     * 查询指定分类的服务
     */
    List<ServiceItem> findByCategory(String category);
    
    /**
     * 查询指定状态的服务（带排序）
     */
    List<ServiceItem> findByStatus(ProductStatus status, Sort sort);
    
    /**
     * 按分类和状态查询服务
     */
    @Query("""
        SELECT s FROM ServiceItem s 
        WHERE s.category = :category 
          AND s.status = :status
        ORDER BY s.priceCents
    """)
    List<ServiceItem> findByCategoryAndStatus(
        @Param("category") String category, 
        @Param("status") ProductStatus status
    );
    
    /**
     * 搜索服务（分类或描述）
     */
    @Query("""
        SELECT s FROM ServiceItem s 
        WHERE (LOWER(s.category) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND s.status = 'ON'
    """)
    List<ServiceItem> searchServices(@Param("keyword") String keyword);
    
    /**
     * 批量更新服务状态
     */
    @Modifying
    @Query("UPDATE ServiceItem s SET s.status = :status WHERE s.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") ProductStatus status);
    
    /**
     * 获取价格范围内的服务
     */
    @Query("""
        SELECT s FROM ServiceItem s 
        WHERE s.priceCents BETWEEN :minPrice AND :maxPrice 
          AND s.status = 'ON'
        ORDER BY s.priceCents
    """)
    List<ServiceItem> findByPriceRange(
        @Param("minPrice") Integer minPrice, 
        @Param("maxPrice") Integer maxPrice
    );
    
    /**
     * 统计各分类服务数量
     */
    @Query("""
        SELECT s.category, COUNT(s) 
        FROM ServiceItem s 
        WHERE s.status = 'ON'
        GROUP BY s.category
    """)
    List<Object[]> countByCategory();
    
    /**
     * 查询热门服务（基于预约次数）
     */
    @Query("""
        SELECT s, COUNT(ai.id) as bookingCount
        FROM ServiceItem s
        LEFT JOIN AppointmentItem ai ON s.id = ai.serviceId
        WHERE s.status = 'ON'
        GROUP BY s.id
        ORDER BY bookingCount DESC
        LIMIT :limit
    """)
    List<Object[]> findPopularServices(@Param("limit") int limit);
    
    /**
     * 更新服务价格
     */
    @Modifying
    @Query("UPDATE ServiceItem s SET s.priceCents = :price WHERE s.id = :id")
    int updatePrice(@Param("id") Long id, @Param("price") Integer price);
    
    /**
     * 查询所有可用服务的分类列表
     */
    @Query("SELECT DISTINCT s.category FROM ServiceItem s WHERE s.status = 'ON' ORDER BY s.category")
    List<String> findAllCategories();
}