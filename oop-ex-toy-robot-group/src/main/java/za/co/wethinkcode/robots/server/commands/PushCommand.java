package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

public class PushCommand implements Command {
    private final Server server;

    public PushCommand(Server server) {
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

            if (!robot.getKind().equalsIgnoreCase("heavy")) {
                response.append("[").append(robot.getName()).append("] Error: Only heavy robots can push!");
                if (i < selectedRobots.size() - 1) response.append("\n--------------------\n");
                continue;
            }

            int dx = 0, dy = 0;
            switch (robot.getDirection().toUpperCase()) {
                case "NORTH": dy = 1; break;
                case "SOUTH": dy = -1; break;
                case "EAST": dx = 1; break;
                case "WEST": dx = -1; break;
            }

            int targetX = robot.getX() + dx;
            int targetY = robot.getY() + dy;

            WorldObject pushedObject = null;
            for (WorldObject obj : world.getObjects()) {
                if (obj.getX() == targetX && obj.getY() == targetY) {
                    pushedObject = obj;
                    break;
                }
            }

            if (pushedObject == null) {
                response.append("[").append(robot.getName()).append("] Pushed empty air!");
            } else {
                int destX = targetX + dx;
                int destY = targetY + dy;

                if (!world.isInsideWorld(destX, destY)) {
                    response.append("[").append(robot.getName()).append("] Cannot push ").append(pushedObject.getType()).append(" off the edge of the world!");
                } else {
                    boolean blockedByAnotherObject = false;
                    for (WorldObject obj : world.getObjects()) {
                        if (obj != pushedObject && obj.getX() == destX && obj.getY() == destY) {
                            blockedByAnotherObject = true;
                            break;
                        }
                    }

                    if (blockedByAnotherObject) {
                        response.append("[").append(robot.getName()).append("] Cannot push ").append(pushedObject.getType()).append(" into another obstacle!");
                    } else {
                        Robot crushedRobot = null;
                        for (Robot other : world.getRobots()) {
                            if (other.getX() == destX && other.getY() == destY) {
                                crushedRobot = other;
                                break;
                            }
                        }

                        pushedObject.setX(destX);
                        pushedObject.setY(destY);

                        response.append("[").append(robot.getName()).append("] Pushed ").append(pushedObject.getType()).append(" forward.");

                        if (crushedRobot != null) {
                            crushedRobot.takeDamage(5); // massive damage
                            response.append(" ").append(crushedRobot.getName()).append(" was crushed! Shields: ").append(crushedRobot.getShields()).append(" | Status: ").append(crushedRobot.getStatus());
                            if (!crushedRobot.isAlive()) {
                                server.removeRobotFromBoard(crushedRobot);
                                response.append(" ").append(crushedRobot.getName()).append(" has been destroyed and removed from the board.");
                            }
                        }
                    }
                }
            }

            if (i < selectedRobots.size() - 1) {
                response.append("\n--------------------\n");
            }
        }
        return response.toString();
    }
}
