package app.nail.interfaces.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** English: Simple health endpoints. */
@RestController
public class HealthController {
    /** English: Liveness probe. */
    @GetMapping("/api/health/live")
    public String live() {
        return "OK";
    }

    /** English: Readiness probe. Extend with DB/cache checks if needed. */
    @GetMapping("/api/health/ready")
    public String ready() {
        return "READY";
    }
}
