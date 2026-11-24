package com.legends.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the Legends game server: a REST API to create games and send actions, plus a
 * WebSocket that pushes each game's events to its players.
 *
 * <p>Run with {@code mvn spring-boot:run}, then open http://localhost:8080.
 *
 * <p>All game rules live in the plain-Java core ({@code com.legends.engine} and below).
 * This package only wraps it for the network and decides what each player may see.
 */
@SpringBootApplication
public class LegendsServer {

    public static void main(String[] args) {
        SpringApplication.run(LegendsServer.class, args);
    }
}
