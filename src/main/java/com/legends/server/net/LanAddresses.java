package com.legends.server.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Finds this computer's addresses on the local network (like 192.168.1.20), so other
 * players on the same Wi-Fi know where to connect.
 *
 * <p>"localhost" only works on the computer running the server. Join links for other
 * players must use one of these addresses instead.
 */
@Component
public class LanAddresses {

    private static final Logger log = LoggerFactory.getLogger(LanAddresses.class);

    /** Private IPv4 addresses of the network interfaces that are up, sorted. Empty if there are none. */
    public List<String> find() {
        List<String> found = new ArrayList<>();
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nic.isUp() || nic.isLoopback() || nic.isVirtual()) {
                    continue;
                }
                for (InetAddress address : Collections.list(nic.getInetAddresses())) {
                    if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                        found.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("Couldn't list network interfaces: {}", e.getMessage());
        }
        Collections.sort(found);
        return found;
    }

    /** Prints where to open the game once the server is listening. */
    @EventListener
    void announce(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        log.info("Legends is running. On this computer, open http://localhost:{}", port);
        for (String address : find()) {
            log.info("Players on your network can open http://{}:{}", address, port);
        }
    }
}
