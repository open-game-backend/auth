package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.test.HttpRequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class AuthControllerIntegrationTests {
    private MockMvc mvc;
    private HttpRequestUtils httpRequestUtils;

    @Autowired
    public AuthControllerIntegrationTests(MockMvc mvc) {
        this.mvc = mvc;

        this.httpRequestUtils = new HttpRequestUtils();
    }

    @Test
    public void givenPlayer_whenLogin_thenOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setKey("testPlayerId");
        request.setProvider("");
        request.setRole(Role.USER);

        httpRequestUtils.assertPostOk(mvc, "/login", request, LoginResponse.class);
    }
}
