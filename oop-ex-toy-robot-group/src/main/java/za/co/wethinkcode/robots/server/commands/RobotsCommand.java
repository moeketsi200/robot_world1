package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;

// Brief: Command to list robots in the world or for a client (console and JSON variants).
public class RobotsCommand {
    private final Server server;

    public RobotsCommand(Server server) {
        this.server = server;
    }

    public void execute() {
        server.listRobots();
    }

    // Handles a JSON request from a connected client
    public String executeForClient(CommandRequest request, Socket clientSocket) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(server.getWorld().getRobots());
        } catch (JsonProcessingException e) {
            return "Error: Could not retrieve robots state.";
        }
    }
}
