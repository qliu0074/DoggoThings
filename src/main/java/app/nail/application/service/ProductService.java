package app.nail.application.service;

import app.nail.common.exception.ApiException;
import app.nail.domain.entity.Product;
import app.nail.domain.entity.ProductImage;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.ProductImageRepository;
import app.nail.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务
 * 职责：
 * 1. 新增/编辑商品与上下架
 * 2. 安全库存操作（pending 冻结与实际扣减）
 * 3. 图片管理（封面与排序）
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductImageRepository imageRepo;

    /** 新建商品（只演示关键字段） */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", allEntries = true)
    public Long createProduct(String name, String category, int priceCents) {
        Product p = Product.builder()
                .name(name)
                .category(category)
                .priceCents(priceCents)
                .stockActual(0)
                .stockPending(0)
                .status(ProductStatus.ON)
                .build();
        return productRepo.save(p).getId();
    }

    /** 修改商品基础信息 */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", key = "#id")
    public void updateProduct(Long id, String name, String category, Integer priceCents, ProductStatus status) {
        Product p = productRepo.findById(id).orElseThrow(() -> ApiException.resourceNotFound("商品不存在"));
        if (name != null) p.setName(name);
        if (category != null) p.setCategory(category);
        if (priceCents != null) p.setPriceCents(priceCents);
        if (status != null) p.setStatus(status);
        productRepo.save(p);
    }

    /** 冻结库存（下单未确认时调用，delta > 0） */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", key = "#productId")
    public void freezeStock(long productId, int delta) {
        lockProduct(productId);
        int ok = productRepo.adjustPendingStock(productId, delta);
        if (ok == 0) throw ApiException.businessViolation("冻结库存失败");
    }

    /** 释放库存（取消时 delta < 0），或减少冻结 */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", key = "#productId")
    public void adjustFrozen(long productId, int delta) {
        lockProduct(productId);
        int ok = productRepo.adjustPendingStock(productId, delta);
        if (ok == 0) throw ApiException.businessViolation("调整冻结库存失败");
    }

    /** 确认扣减实际库存（发货或确认时） */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", key = "#productId")
    public void confirmDeduct(long productId, int qty) {
        lockProduct(productId);
        int ok = productRepo.tryDeductActualStock(productId, qty);
        if (ok == 0) throw ApiException.businessViolation("实际库存不足");
    }

    /** English: Restore actual stock after refunds. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", key = "#productId")
    public void restoreStock(long productId, int qty) {
        if (qty <= 0) {
            throw ApiException.businessViolation("返还库存数量必须为正");
        }
        lockProduct(productId);
        int ok = productRepo.increaseActualStock(productId, qty);
        if (ok == 0) {
            throw ApiException.businessViolation("返还库存失败");
        }
    }

    /** 添加图片并可选设为封面 */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(cacheNames = "product-detail", key = "#productId")
    public Long addImage(long productId, String url, boolean cover, short sortOrder) {
        Product p = lockProduct(productId);
        if (cover && imageRepo.existsByProductIdAndCoverTrue(productId)) {
            throw ApiException.conflict("封面已存在");
        }
        ProductImage img = ProductImage.builder()
                .product(p)
                .imageUrl(url)
                .cover(cover)
                .sortOrder(sortOrder)
                .build();
        return imageRepo.save(img).getId();
    }

    /** 获取商品图片（排序） */
    public List<ProductImage> listImages(long productId) {
        return imageRepo.findByProductIdOrderBySortOrderAsc(productId);
    }

    /**
     * English: Cached product lookup for high-frequency reads.
     */
    @Cacheable(cacheNames = "product-detail", key = "#id")
    public Product getProduct(long id) {
        return productRepo.findById(id).orElseThrow(() -> ApiException.resourceNotFound("商品不存在"));
    }

    private Product lockProduct(long productId) {
        return productRepo.lockById(productId)
                .orElseThrow(() -> ApiException.resourceNotFound("商品不存在"));
    }
}
