// English comments only inside code.

// Package: keep the same as your project structure.
package app.nail.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

@Configuration
public class JacksonConfig {

    /**
     * English: Incremental customization for the primary ObjectMapper managed by Spring Boot.
     * Do NOT set timezone or date format here, so properties in application.yml take effect.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return (Jackson2ObjectMapperBuilder builder) -> {
            // English: Support java.time.* (LocalDateTime, Instant, etc.)
            builder.modules(new JavaTimeModule());

            // English: Do not write dates as timestamps (e.g., 1712345678). Use textual ISO by default.
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // English: Ignore unknown JSON properties instead of failing deserialization.
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            // English: Exclude null fields from JSON output for cleaner responses.
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);

            // English: Keep property naming strategy default; let application.yml control timezone/format.
        };
    }
}
