package app.nail.application.service;

import app.nail.domain.entity.ProductImage;
import app.nail.domain.entity.ServiceImage;
import app.nail.domain.repository.ProductImageRepository;
import app.nail.domain.repository.ServiceImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 图片服务（可选）
 * 职责：
 * 1. 统一设置封面（确保同一商品/服务仅一张封面）
 * 2. 可拓展为接入对象存储（返回可访问 URL）
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ProductImageRepository productImageRepo;
    private final ServiceImageRepository serviceImageRepo;

    /** 设置商品封面：将其他封面清除，当前设为封面 */
    @Transactional
    public void setProductCover(long imageId) {
        ProductImage img = productImageRepo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        // 简单做法：清除该商品其他封面
        productImageRepo.findByProductIdOrderBySortOrderAsc(img.getProduct().getId())
                .forEach(i -> { i.setCover(i.getId().equals(imageId)); productImageRepo.save(i); });
    }

    /** 设置服务封面：逻辑同上 */
    @Transactional
    public void setServiceCover(long imageId) {
        ServiceImage img = serviceImageRepo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        serviceImageRepo.findByServiceIdOrderBySortOrderAsc(img.getService().getId())
                .forEach(i -> { i.setCover(i.getId().equals(imageId)); serviceImageRepo.save(i); });
    }
}
