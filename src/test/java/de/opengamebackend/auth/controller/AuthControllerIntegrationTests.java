package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import de.opengamebackend.test.HttpRequestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
    private TestEntityManager entityManager;
    private HttpRequestUtils httpRequestUtils;

    @Autowired
    public AuthControllerIntegrationTests(MockMvc mvc, TestEntityManager entityManager) {
        this.mvc = mvc;
        this.entityManager = entityManager;

        this.httpRequestUtils = new HttpRequestUtils();
    }

    @Test
    public void whenRegister_thenOk() throws Exception {
        RegisterRequest request = new RegisterRequest("testNickname");
        httpRequestUtils.assertPostOk(mvc, "/register", request, RegisterResponse.class);
    }

    @Test
    public void givenPlayer_whenLogin_thenOk() throws Exception {
        Player player = new Player("testId", "testNickname");
        entityManager.persistAndFlush(player);

        LoginRequest request = new LoginRequest(player.getPlayerId());
        httpRequestUtils.assertPostOk(mvc, "/login", request, LoginResponse.class);
    }
}
