package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.requests.LockPlayerRequest;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.UnlockPlayerRequest;
import de.opengamebackend.auth.model.responses.GetAdminsResponse;
import de.opengamebackend.auth.model.responses.LockPlayerResponse;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.auth.model.responses.UnlockPlayerResponse;
import de.opengamebackend.test.HttpRequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.UUID;

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
    public void whenGetAdmins_thenOk() throws Exception {
        httpRequestUtils.assertGetOk(mvc, "/admins", GetAdminsResponse.class);
    }

    @Test
    public void givenPlayer_whenLogin_thenOk() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setKey("testPlayerId");
        request.setProvider("");
        request.setRole(Role.USER);

        httpRequestUtils.assertPostOk(mvc, "/login", request, LoginResponse.class);
    }

    @Test
    public void givenPlayer_whenLockPlayer_thenOk() throws Exception {
        Player player = new Player();
        player.setId(UUID.randomUUID().toString());
        player.setProvider("testProvider");
        player.setProviderUserId("testProviderUserId");
        entityManager.persistAndFlush(player);

        LockPlayerRequest request = new LockPlayerRequest();
        request.setProvider(player.getProvider());
        request.setProviderUserId(player.getProviderUserId());

        httpRequestUtils.assertPostOk(mvc, "/lockPlayer", request, LockPlayerResponse.class);
    }

    @Test
    public void givenPlayer_whenUnlockPlayer_thenOk() throws Exception {
        Player player = new Player();
        player.setId(UUID.randomUUID().toString());
        player.setProvider("testProvider");
        player.setProviderUserId("testProviderUserId");
        entityManager.persistAndFlush(player);

        UnlockPlayerRequest request = new UnlockPlayerRequest();
        request.setProvider(player.getProvider());
        request.setProviderUserId(player.getProviderUserId());

        httpRequestUtils.assertPostOk(mvc, "/unlockPlayer", request, UnlockPlayerResponse.class);
    }
}
