package app.nail.interfaces.client.controller;

import app.nail.domain.entity.ServiceItem;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.ServiceItemRepository;
import app.nail.interfaces.client.dto.ClientServiceDtos.ServiceResp;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

/** English: Client service browsing controller. */
@RestController
@RequestMapping("/api/client/services")
@RequiredArgsConstructor
public class ClientServiceController {

    private final ServiceItemRepository serviceRepo;

    /** English: Paged service list for clients, only ON. */
    @GetMapping
    public PageResp<ServiceResp> page(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        var p = (category != null && !category.isBlank())
                ? serviceRepo.findByCategoryAndStatus(category, ProductStatus.ON, pageable)
                : serviceRepo.findByStatus(ProductStatus.ON, pageable);
        return new PageResp<>(
                p.map(this::toResp).getContent(),
                p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize()
        );
    }

    /** English: Service detail. */
    @GetMapping("/{id}")
    public ServiceResp get(@PathVariable Long id) {
        ServiceItem e = serviceRepo.findById(id).orElseThrow(() -> new RuntimeException("service not found"));
        return toResp(e);
    }

    private ServiceResp toResp(ServiceItem e) {
        return new ServiceResp(e.getId(), e.getCategory(), e.getPriceCents(),
                e.getDescription(), e.getStatus(), e.getUpdatedAt());
    }
}
