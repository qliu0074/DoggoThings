package app.nail.integration;

import app.nail.interfaces.client.controller.ClientOrderController;
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

/** English: Smoke test for client order endpoint with security on. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientOrderControllerIT {

    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "u1", roles = {"CLIENT"}) // English: bypass JWT for test
    void listOrders_shouldReturn200() throws Exception {
        mvc.perform(get("/api/client/orders")
                .param("userId", "1")
                .param("page","0").param("size","5")
                .accept(APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
    }
}
