package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

// Brief: Command to inspect surroundings from the robot's perspective and report visible objects.
public class LookCommand implements Command {
    private final Server server;

    public LookCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        StringBuilder look = new StringBuilder();
        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot robot = selectedRobots.get(i);
            look.append("[").append(robot.getName()).append("] Looking ")
                    .append(robot.getDirection()).append("\n")
                    .append(describeVisibleObjects(robot));
            if (i < selectedRobots.size() - 1) {
                look.append("\n--------------------\n");
            }
        }
        return look.toString();
    }

    private String describeVisibleObjects(Robot robot) {
        StringBuilder visible = new StringBuilder();
        World world = server.getWorld();
        int maxDist = world.getVisibility(); // How far the robot can see
        String[] dirs = {"NORTH", "SOUTH", "EAST", "WEST"};

        for (String dir : dirs) {
            int dx = 0, dy = 0;
            switch(dir) {
                case "NORTH": dy = 1; break;
                case "SOUTH": dy = -1; break;
                case "EAST": dx = 1; break;
                case "WEST": dx = -1; break;
            }

            int cx = robot.getX();
            int cy = robot.getY();

            for (int step = 1; step <= maxDist; step++) {
                cx += dx;
                cy += dy;

                if (!world.isInsideWorld(cx, cy)) {
                    visible.append("- Edge at ").append(step).append(" steps ").append(dir).append("\n");
                    break;
                }

                WorldObject hitObj = null;
                for (WorldObject obj : world.getObjects()) {
                    if (obj.getX() == cx && obj.getY() == cy) { hitObj = obj; break; }
                }
                if (hitObj != null) {
                    visible.append("- ").append(hitObj.getType()).append(" at ").append(step).append(" steps ").append(dir).append("\n");
                    break;
                }

                Robot hitRob = null;
                for (Robot other : world.getRobots()) {
                    if (other != robot && other.getX() == cx && other.getY() == cy) { hitRob = other; break; }
                }
                if (hitRob != null) {
                    visible.append("- Robot ").append(hitRob.getName()).append(" at ").append(step).append(" steps ").append(dir).append("\n");
                    break;
                }
            }
        }
        if (visible.length() == 0) {
            return "Nothing visible within " + maxDist + " steps.";
        }
        return visible.toString().trim();
    }
}