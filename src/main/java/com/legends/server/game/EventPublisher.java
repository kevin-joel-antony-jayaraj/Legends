package com.legends.server.game;

import com.legends.server.view.EventView;
import java.util.List;

/**
 * Sends a game's events to everyone watching it.
 *
 * <p>An interface so the service doesn't depend on WebSockets directly: tests can swap in
 * a recording publisher, and another transport could replace STOMP later.
 */
public interface EventPublisher {

    void publish(String gameId, List<EventView> events);
}
