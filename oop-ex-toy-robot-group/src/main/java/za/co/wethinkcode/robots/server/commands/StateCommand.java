package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

// Brief: Command to report the current state of selected robots.
public class StateCommand implements Command {
    private final Server server;

    public StateCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (!selectedRobots.isEmpty()) {
            return formatRobotList(selectedRobots);
        }
        return "Error: You must launch a robot first.";
    }

    private String formatRobotList(List<Robot> selectedRobots) {
        StringBuilder allStates = new StringBuilder();
        for (int i = 0; i < selectedRobots.size(); i++) {
            allStates.append(selectedRobots.get(i));
            if (i < selectedRobots.size() - 1) {
                allStates.append("\n--------------------\n");
            }
        }
        return allStates.toString();
    }
}