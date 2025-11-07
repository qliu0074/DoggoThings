package app.nail.interfaces.admin.controller;

import app.nail.application.service.ServiceItemService;
import app.nail.domain.entity.ServiceItem;
import app.nail.domain.repository.ServiceItemRepository;
import app.nail.interfaces.admin.dto.AdminServiceDtos.*;
import app.nail.interfaces.common.PageRequestParams;
import app.nail.interfaces.common.dto.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** English: Admin service management. */
@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

    private final ServiceItemService serviceItemService;
    private final ServiceItemRepository serviceRepo;

    @PostMapping
    public Long create(@RequestBody CreateServiceReq req) {
        return serviceItemService.createService(req.category(), req.priceCents(), req.description());
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody UpdateServiceReq req) {
        serviceItemService.updateService(id, req.category(), req.priceCents(), req.description(), req.status());
    }

    @GetMapping
    public PageResp<ServiceItem> page(@RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size) {
        PageRequestParams params = PageRequestParams.of(page, size);
        Pageable pageable = PageRequest.of(params.page(), params.size(), Sort.by("updatedAt").descending());
        var p = serviceRepo.findAll(pageable);
        return new PageResp<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @PostMapping("/{id}/images")
    public Long addImage(@PathVariable long id, @RequestBody AddImageReq req) {
        return serviceItemService.addImage(id, req.url(), req.cover(), req.sortOrder());
    }
}
