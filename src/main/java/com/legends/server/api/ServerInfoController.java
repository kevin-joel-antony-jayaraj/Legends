package com.legends.server.api;

import com.legends.server.net.LanAddresses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tells the lobby page how other computers can reach this server, so it can build join
 * links that work across the local network instead of "localhost" ones.
 */
@RestController
public class ServerInfoController {

    /**
     * @param lanAddresses this computer's addresses on the local network
     * @param port         the port the browser used to reach the server
     */
    public record ServerInfo(List<String> lanAddresses, int port) {
    }

    private final LanAddresses lan;

    public ServerInfoController(LanAddresses lan) {
        this.lan = lan;
    }

    @GetMapping("/api/server")
    public ServerInfo info(HttpServletRequest request) {
        return new ServerInfo(lan.find(), request.getServerPort());
    }
}
