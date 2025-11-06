package app.nail.application.service;

import app.nail.domain.entity.ServiceImage;
import app.nail.domain.entity.ServiceItem;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.ServiceImageRepository;
import app.nail.domain.repository.ServiceItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务项目服务
 * 职责：
 * 1. 新增/编辑服务项目与上下架
 * 2. 服务图片管理
 */
@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceItemRepository serviceRepo;
    private final ServiceImageRepository imageRepo;

    @Transactional
    public Long createService(String category, int priceCents, String description) {
        ServiceItem s = ServiceItem.builder()
                .category(category)
                .priceCents(priceCents)
                .description(description)
                .status(ProductStatus.ON)
                .build();
        return serviceRepo.save(s).getId();
    }

    @Transactional
    public void updateService(Long id, String category, Integer priceCents, String description, ProductStatus status) {
        ServiceItem s = serviceRepo.findById(id).orElseThrow(() -> new RuntimeException("服务不存在"));
        if (category != null) s.setCategory(category);
        if (priceCents != null) s.setPriceCents(priceCents);
        if (description != null) s.setDescription(description);
        if (status != null) s.setStatus(status);
        serviceRepo.save(s);
    }

    @Transactional
    public Long addImage(long serviceId, String url, boolean cover, short sortOrder) {
        ServiceItem s = serviceRepo.findById(serviceId).orElseThrow(() -> new RuntimeException("服务不存在"));
        if (cover && imageRepo.existsByServiceIdAndCoverTrue(serviceId)) {
            throw new RuntimeException("封面已存在");
        }
        ServiceImage img = ServiceImage.builder()
                .service(s)
                .imageUrl(url)
                .cover(cover)
                .sortOrder(sortOrder)
                .build();
        return imageRepo.save(img).getId();
    }

    public List<ServiceImage> listImages(long serviceId) {
        return imageRepo.findByServiceIdOrderBySortOrderAsc(serviceId);
    }
}
