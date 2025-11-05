package app.nail.interfaces.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** English: Admin reports placeholder. */
@RestController
public class ReportAdminController {
    @GetMapping("/api/admin/reports/health")
    public Map<String,Object> health() { return Map.of("ok", true); }
}
