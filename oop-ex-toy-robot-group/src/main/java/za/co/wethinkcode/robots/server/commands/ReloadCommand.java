package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

// Brief: Command to reload weapons for selected robots, respecting reload times.
public class ReloadCommand implements Command {
    private final Server server;

    public ReloadCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        StringBuilder response = new StringBuilder();
        World world = server.getWorld();
        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot robot = selectedRobots.get(i);
            if (!robot.isAlive()) {
                response.append("[").append(robot.getName()).append("] Cannot reload a dead robot.");
            } else {
                String kindKey = robot.getKind().toLowerCase();
                int reloadMs = world.getReloadTimes().getOrDefault(kindKey, 1000);
                robot.setStatus("RELOAD");
                waitForTimer(reloadMs);
                robot.reloadWeapon();
                robot.setStatus("NORMAL");
                robot.setNextAllowedFireAtMillis(System.currentTimeMillis());
                response.append("[")
                        .append(robot.getName())
                        .append("] Reload complete. Shots restored to ")
                        .append(robot.getShots())
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
