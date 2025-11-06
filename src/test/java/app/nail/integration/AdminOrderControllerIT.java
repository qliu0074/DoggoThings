package app.nail.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** English: Admin access requires role ADMIN. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminOrderControllerIT {

    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void adminList_shouldReturn200() throws Exception {
        mvc.perform(get("/api/admin/orders").param("page","0").param("size","5")
                .accept(APPLICATION_JSON))
           .andExpect(status().isOk());
    }
}
