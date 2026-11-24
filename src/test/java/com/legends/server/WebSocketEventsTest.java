package com.legends.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legends.server.api.GameController;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * Starts a real server on a random port, subscribes to a game's topic over WebSocket,
 * and checks that an action's events arrive there.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketEventsTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ObjectMapper json;

    @Test
    void everySubscriberReceivesTheEventsOfAnAction() throws Exception {
        JsonNode created = json.readTree(http.postForObject("/api/games", jsonBody("""
                {"seed": 5, "players": [{"name": "Ana", "hero": "ARCHER"}, {"name": "Ben", "hero": "KNIGHT"}]}"""),
                String.class));
        String gameId = created.get("gameId").asText();

        BlockingQueue<JsonNode> received = new LinkedBlockingQueue<>();
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession session = client.connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
        }).get(5, TimeUnit.SECONDS);
        session.subscribe("/topic/games/" + gameId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return JsonNode.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((JsonNode) payload);
            }
        });
        Thread.sleep(200); // give the subscription a moment to register

        String token = tokenOfCurrentPlayer(created, gameId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(GameController.TOKEN_HEADER, token);
        http.postForObject("/api/games/" + gameId + "/actions",
                new HttpEntity<>("{\"type\": \"END_TURN\"}", headers), String.class);

        JsonNode events = received.poll(5, TimeUnit.SECONDS);
        assertThat(events).as("events pushed over the WebSocket").isNotNull();
        assertThat(events.get(0).get("type").asText()).isEqualTo("TurnStarted");
        session.disconnect();
    }

    private String tokenOfCurrentPlayer(JsonNode created, String gameId) throws Exception {
        String anyToken = created.get("seats").get(0).get("token").asText();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GameController.TOKEN_HEADER, anyToken);
        JsonNode view = json.readTree(http.exchange("/api/games/" + gameId, org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), String.class).getBody());
        int current = view.get("currentSeat").asInt();
        return created.get("seats").get(current).get("token").asText();
    }

    private static HttpEntity<String> jsonBody(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
