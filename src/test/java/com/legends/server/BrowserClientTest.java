package com.legends.server;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The server hands out the browser client: the lobby at "/", the game page, and the
 * address info the lobby uses to build join links. (The client's own logic is tested
 * with Node in src/test/js.)
 */
@SpringBootTest
@AutoConfigureMockMvc
class BrowserClientTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void theRootOpensTheLobby() throws Exception {
        mvc.perform(get("/")).andExpect(forwardedUrl("index.html"));
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("js/lobby.js")));
    }

    @Test
    void theGamePageAndItsScriptsAreServed() throws Exception {
        mvc.perform(get("/play.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<canvas id=\"board\"")));
        for (String script : new String[] {"play", "api", "stomp", "turn-controls", "board-renderer", "panels"}) {
            mvc.perform(get("/js/" + script + ".js")).andExpect(status().isOk());
        }
    }

    @Test
    void serverInfoListsAddressesAndThePortUsed() throws Exception {
        mvc.perform(get("/api/server"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lanAddresses").isArray())
                .andExpect(jsonPath("$.port").value(80));
    }
}
