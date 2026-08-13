package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

// Command to move robot backward by specified steps
// Brief: Moves selected robots backward a given number of steps, with collision checks.

import java.net.Socket;
import java.util.List;

import za.co.wethinkcode.robots.protocol.CommandRequest;

public class BackCommand implements Command {
    private final Server server;

    public BackCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        int steps = 1;
        List<String> args = request.getArguments();
        String arg = "";
        if (args != null && !args.isEmpty()) {
            arg = args.get(args.size() - 1);
        } else if (request.getRobot() != null && !request.getRobot().isBlank()) {
            String[] parts = request.getRobot().trim().split("\\s+");
            arg = parts[parts.length - 1];
        }

        if (!arg.isBlank() && arg.matches("-?\\d+")) {
            steps = Integer.parseInt(arg);
        } else if (!arg.isBlank() || (args != null && args.size() > 1)) {
            return "Error: Invalid number of steps.";
        }

        StringBuilder response = new StringBuilder();
        World world = server.getWorld();
        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot robot = selectedRobots.get(i);
            int dx = 0, dy = 0;
            // Backwards movement inverts the logic
            switch (robot.getDirection().toUpperCase()) {
                case "NORTH": dy = -1; break;
                case "SOUTH": dy = 1; break;
                case "EAST": dx = -1; break;
                case "WEST": dx = 1; break;
            }

            int moved = 0;
            String blockReason = null;
            String mineEvent = null;
            
            // Step-by-step collision check
            for (int s = 0; s < steps; s++) {
                int nx = robot.getX() + dx;
                int ny = robot.getY() + dy;
                
                if (!world.isInsideWorld(nx, ny)) {
                    blockReason = "Edge of world"; break;
                }
                
                boolean obstructed = false;
                for (WorldObject obj : world.getObjects()) {
                    if (obj.getX() == nx && obj.getY() == ny) { obstructed = true; break; }
                }
                for (Robot other : world.getRobots()) {
                    if (other != robot && other.getX() == nx && other.getY() == ny) { obstructed = true; break; }
                }
                
                if (obstructed) {
                    blockReason = "Obstacle or Robot"; break;
                }
                
                robot.setPosition(nx, ny);
                moved++;

                WorldObject triggeredMine = null;
                for (WorldObject mine : world.getMines()) {
                    if (robot.getName().equals(mine.getOwner())) {
                        continue;
                    }
                    int dist = Math.abs(mine.getX() - nx) + Math.abs(mine.getY() - ny);
                    if (dist <= 4) {
                        triggeredMine = mine;
                        break;
                    }
                }
                
                if (triggeredMine != null) {
                    world.removeMine(triggeredMine);
                    robot.takeDamage(5); // massive damage
                    mineEvent = " Triggered a proximity mine! Took 5 damage.";
                    if (!robot.isAlive()) {
                        server.removeRobotFromBoard(robot);
                        mineEvent += " Destroyed by mine!";
                        break; // Stop movement
                    }
                }
            }

            response.append("[").append(robot.getName()).append("] Moved back ").append(moved).append(" steps.");
            if (blockReason != null) {
                response.append(" Stopped due to: ").append(blockReason).append(".");
            }
            if (mineEvent != null) {
                response.append(mineEvent);
            }
            if (i < selectedRobots.size() - 1) {
                response.append("\n--------------------\n");
            }
        }
        return response.toString();
    }
}
