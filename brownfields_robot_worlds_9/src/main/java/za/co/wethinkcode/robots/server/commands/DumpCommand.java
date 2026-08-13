package za.co.wethinkcode.robots.server.commands;

// Fix: Import World from the world package
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.World;
import za.co.wethinkcode.robots.server.Robot;

// SERVER-SIDE dump command.
// Typed into the server's terminal (not sent by a robot client).
//
// What it does:
// - Prints the world size and settings
// - Lists every obstacle in the world
// - Lists every robot and their full state
//
// Usage: type "dump" in the server terminal
public class DumpCommand {

    public void execute(World world) {

        System.out.println("+------------------------------------------+");
        System.out.println("|              WORLD DUMP                  |");
        System.out.println("+------------------------------------------+");

        // ── World settings ──────────────────────────────────────────────────
        System.out.println("  World settings:");
        System.out.println("    Size       : " + world.getWidth() + " x " + world.getHeight());
        System.out.println("    Visibility : " + world.getVisibility() + " steps");
        System.out.println();

        // ── Obstacles ───────────────────────────────────────────────────────
        System.out.println("  Obstacles (" + world.getObstacles().size() + "):");

        if (world.getObstacles().isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (Position pos : world.getObstacles()) {
                System.out.println("    (" + pos.getX() + "," + pos.getY() + ")");
            }
        }
        System.out.println();

        // ── Robots ──────────────────────────────────────────────────────────
        System.out.println("  Robots (" + world.getAllRobots().size() + "):");

        if (world.getAllRobots().isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (Robot robot : world.getAllRobots()) {
                System.out.println("    " + robot);
            }
        }

        System.out.println("+------------------------------------------+");
    }
}