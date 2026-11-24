package com.legends.server.game;

import com.legends.server.view.EventView;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Publishes events to the STOMP topic {@code /topic/games/{gameId}}. */
@Component
public class StompEventPublisher implements EventPublisher {

    private final SimpMessagingTemplate messaging;

    public StompEventPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Override
    public void publish(String gameId, List<EventView> events) {
        messaging.convertAndSend("/topic/games/" + gameId, events);
    }
}
