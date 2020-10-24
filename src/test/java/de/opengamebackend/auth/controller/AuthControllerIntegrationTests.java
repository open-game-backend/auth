package de.opengamebackend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class AuthControllerIntegrationTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void whenRegister_thenOk() throws Exception {
        RegisterRequest request = new RegisterRequest("testNickname");
        httpPost("/register", request, RegisterResponse.class);
    }

    @Test
    public void givenPlayer_whenLogin_thenOk() throws Exception {
        Player player = new Player("testId", "testNickname");
        entityManager.persist(player);
        entityManager.flush();

        LoginRequest request = new LoginRequest(player.getPlayerId());
        httpPost("/login", request, LoginResponse.class);
    }

    private <T> T httpPost(String url, Object request, Class<T> responseClass) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);

        String responseJson = mvc.perform(post(url)
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        T response = objectMapper.readValue(responseJson, responseClass);
        assertThat(response).isNotNull();

        return response;
    }
}
