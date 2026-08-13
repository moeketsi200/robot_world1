package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

// SERVER-SIDE robots command.
// Typed into the server's terminal (not sent by a robot client).
//
// What it does:
// - Lists every robot currently in the world
// - Shows each robot's name, position, direction, shields, shots and status
//
// Usage: type "robots" in the server terminal
public class RobotsCommand {

    public void execute(World world) {

        // Get all robots currently in the world
        var robots = world.getAllRobots();

        System.out.println("=== Robots in the world ===");

        // If nobody is connected, say so
        if (robots.isEmpty()) {
            System.out.println("  (no robots connected)");
            return;
        }

        // Print one line of info per robot
        for (Robot robot : robots) {
            System.out.println(
                    "  Name     : " + robot.getName()                          + "\n" +
                            "  Make     : " + robot.getKind()                          + "\n" +
                            "  Position : " + robot.getPosition()                      + "\n" +
                            "  Facing   : " + robot.getDirection()                     + "\n" +
                            "  Shields  : " + robot.getShields() + "/" + robot.getMaxShields() + "\n" +
                            "  Shots    : " + robot.getShots()   + "/" + robot.getMaxShots()   + "\n" +
                            "  Status   : " + robot.getStatus()                        + "\n" +
                            "  ----"
            );
        }

        System.out.println("Total: " + robots.size() + " robot(s)");
    }
}
