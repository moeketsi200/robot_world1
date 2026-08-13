package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

/**
 * The FireCommand handles the logic for a robot shooting a weapon.
 * It calculates the trajectory of the bullet up to a maximum distance (e.g., 5 steps),
 * checking at each step if the bullet hits the edge of the world, an obstacle, 
 * or another robot. It also manages deducting ammo (shots) from the firing robot.
 */
// Brief: Command to fire a robot's weapon at a target, applying damage as needed.
public class FireCommand implements Command {
    private final Server server;

    public FireCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        // 1. Verify that the client actually has a robot launched in the world.
        // A client cannot fire if they haven't launched a robot yet.
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        StringBuilder response = new StringBuilder();
        World world = server.getWorld();

        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot robot = selectedRobots.get(i);

            if (robot.getShots() <= 0) {
                response.append("[").append(robot.getName()).append("] Out of shots!");
                if (i < selectedRobots.size() - 1) {
                    response.append("\n--------------------\n");
                }
                continue;
            }

            // Enforce weapon reload time (cooldown) before firing
            String kindKey = robot.getKind().toLowerCase();
            int reloadMs = world.getReloadTimes().getOrDefault(kindKey, 1000);
            long now = System.currentTimeMillis();
            if (now < robot.getNextAllowedFireAtMillis()) {
                long remaining = robot.getNextAllowedFireAtMillis() - now;
                response.append("[")
                        .append(robot.getName())
                        .append("] Weapon reloading (" )
                        .append(remaining)
                        .append("ms remaining). Missing shot.");
                if (i < selectedRobots.size() - 1) {
                    response.append("\n--------------------\n");
                }
                continue;
            }

            robot.fireWeapon();
            robot.setNextAllowedFireAtMillis(now + reloadMs);

            if (robot.getKind().equalsIgnoreCase("bomber")) {
                WorldObject mine = new WorldObject(robot.getX(), robot.getY(), "Mine");
                mine.setOwner(robot.getName());
                world.addMine(mine);
                response.append("[").append(robot.getName()).append("] Placed a hidden proximity mine at [")
                        .append(robot.getX()).append(", ").append(robot.getY()).append("].");
                if (i < selectedRobots.size() - 1) {
                    response.append("\n--------------------\n");
                }
                continue;
            }

            int bulletDistance;

            switch (robot.getKind().toLowerCase()) {
                case "sniper": bulletDistance = 10; break;
                case "heavy":  bulletDistance = 3; break;
                case "normal":
                default:       bulletDistance = 5; break;
            }
            int currentX = robot.getX();
            int currentY = robot.getY();

            String direction = robot.getDirection().toUpperCase();

            int dx = 0;
            int dy = 0;

            switch (direction) {
                case "NORTH": dy = 1; break;
                case "SOUTH": dy = -1; break;
                case "EAST": dx = 1; break;
                case "WEST": dx = -1; break;
            }

            boolean hit = false;
            String hitObject = "";
            int hitDistance = 0;
            Robot hitRobot = null;

            // 5. Ray-casting loop: Move the bullet one step at a time and check for collisions
            for (int step = 1; step <= bulletDistance; step++) {
                // Move the bullet forward by 1 step
                currentX += dx;
                currentY += dy;

                // Collision Check A: Did the bullet fly off the edge of the world?
                if (!world.isInsideWorld(currentX, currentY)) {
                    hitObject = "Edge of world";
                    hit = true;
                    hitDistance = step;
                    break; // Stop bullet movement
                }

                // Collision Check B: Did the bullet hit a stationary obstacle?
                for (WorldObject obj : world.getObjects()) {
                    if (obj.getX() == currentX && obj.getY() == currentY) {
                        hit = true;
                        hitObject = obj.getType();
                        hitDistance = step;
                        break;
                    }
                }
                if (hit) break;

                // Collision Check C: Did the bullet hit another robot?
                for (Robot other : world.getRobots()) {
                    if (other != robot && other.getX() == currentX && other.getY() == currentY) {
                        hit = true;
                        hitRobot = other;
                        hitObject = "Robot " + other.getName();
                        hitDistance = step;
                        break;
                    }
                }
                if (hit) break;
            }

            // 6. Apply damage if a robot was hit and construct the final response message
            if (hit && hitRobot != null) {
                hitRobot.takeDamage();
                response.append("[").append(robot.getName()).append("] Fired! ");
                response.append("Hit ").append(hitObject).append(" at [").append(currentX).append(", ").append(currentY).append("] (Distance: ").append(hitDistance).append(").");
                response.append(" ").append(hitRobot.getName()).append(" took damage! Shields: ").append(hitRobot.getShields()).append(" | Status: ").append(hitRobot.getStatus());
                if (!hitRobot.isAlive()) {
                    server.removeRobotFromBoard(hitRobot);
                    response.append(" ").append(hitRobot.getName()).append(" has been destroyed and removed from the board.");
                }
            } else {
                response.append("[").append(robot.getName()).append("] Fired! ");
                if (hit) {
                    response.append("Hit ").append(hitObject).append(" at [").append(currentX).append(", ").append(currentY).append("] (Distance: ").append(hitDistance).append(").");
                } else {
                    response.append("Missed.");
                }
            }

            if (i < selectedRobots.size() - 1) {
                response.append("\n--------------------\n");
            }
        }
        return response.toString();
    }
}
