package io.tohk3n.bcm.api.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProxyController.class)
@Import(PathValidator.class)
class ProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BitjitaClient bitjitaClient;

    @Test
    void returnsUpstreamResponse() throws Exception {
        when(bitjitaClient.fetch("/items"))
                .thenReturn(ResponseEntity.ok("[{\"id\":1}]"));

        mockMvc.perform(get("/api/proxy").param("path", "/items"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1}]"))
                .andExpect(header().string("Cache-Control", "s-maxage=60"));
    }

    @Test
    void returns400WhenPathMissing() throws Exception {
        mockMvc.perform(get("/api/proxy"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"Missing path parameter\"}"));
    }

    @Test
    void returns403WhenPathNotAllowed() throws Exception {
        mockMvc.perform(get("/api/proxy").param("path", "/admin/secrets"))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"error\":\"Path not allowed\"}"));
    }

    @Test
    void returns502WhenUpstreamFails() throws Exception {
        when(bitjitaClient.fetch(anyString()))
                .thenThrow(new RestClientException("Connection refused"));

        mockMvc.perform(get("/api/proxy").param("path", "/items"))
                .andExpect(status().is(502))
                .andExpect(content().json("{\"error\":\"Upstream request failed\"}"));
    }

    @Test
    void passesQueryStringToUpstream() throws Exception {
        when(bitjitaClient.fetch("/claims?q=ardent&limit=10"))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/api/proxy").param("path", "/claims?q=ardent&limit=10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}