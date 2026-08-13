package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

// Command to turn robot left (update facing direction)
// Brief: Turns selected robots one step to the left (counter-clockwise).

import java.net.Socket;
import java.util.List;

import za.co.wethinkcode.robots.protocol.CommandRequest;

public class LeftCommand implements Command {
    private final Server server;

    public LeftCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        String[] dirs = {"NORTH", "EAST", "SOUTH", "WEST"};
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot robot = selectedRobots.get(i);
            int idx = java.util.Arrays.asList(dirs).indexOf(robot.getDirection().toUpperCase());
            if (idx == -1) idx = 0;
            robot.setDirection(dirs[(idx + 3) % 4]);

            response.append("[").append(robot.getName()).append("] Turned left. Now facing ").append(robot.getDirection()).append(".");
            if (i < selectedRobots.size() - 1) {
                response.append("\n--------------------\n");
            }
        }
        return response.toString();
    }
}
