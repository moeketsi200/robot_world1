package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

// Brief: Command to repair shields for selected robots, restoring shield strength.
public class RepairCommand implements Command {
    private static final int REPAIR_TIME_MS = 1000;

    private final Server server;

    public RepairCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        StringBuilder response = new StringBuilder();
        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot robot = selectedRobots.get(i);
            if (!robot.isAlive()) {
                response.append("[").append(robot.getName()).append("] Cannot repair a dead robot.");
            } else {
                robot.setStatus("REPAIR");
                waitForTimer(REPAIR_TIME_MS);
                robot.repairShields();
                response.append("[")
                        .append(robot.getName())
                        .append("] Repair complete. Shields restored to ")
                        .append(robot.getShields())
                        .append(".");
            }

            if (i < selectedRobots.size() - 1) {
                response.append("\n--------------------\n");
            }
        }
        return response.toString();
    }

    private void waitForTimer(int timerMs) {
        try {
            Thread.sleep(timerMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
