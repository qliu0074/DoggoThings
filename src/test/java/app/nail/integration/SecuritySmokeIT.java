package app.nail.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** English: Verify security rules quickly. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecuritySmokeIT {

    @Autowired MockMvc mvc;

    @Test
    void adminShouldBeProtected() throws Exception {
        mvc.perform(get("/api/admin/orders").accept(APPLICATION_JSON))
           .andExpect(status().isUnauthorized()); // 401 without auth
    }

    @Test
    void healthShouldBeOpen() throws Exception {
        mvc.perform(get("/api/health/live"))
           .andExpect(status().isOk())
           .andExpect(content().string("OK"));
    }
}
