package app.nail.interfaces.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** English: Simple liveness endpoint. */
@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> ok() { return Map.of("status", "UP"); }
}
