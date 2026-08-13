package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.world.World;
//
//// SERVER-SIDE quit command.
//// Typed into the server's terminal (not sent by a robot client).
////
//// What it does:
//// - Removes all robots from the world
//// - Prints a goodbye message
//// - Shuts the whole server down
////
//// Usage: type "quit" in the server terminal
public class ServerQuitCommand {

    // No robotName needed here — this is a server console command, not a robot command
    public void execute(World world) {

        System.out.println("Shutting down the world...");

        // Remove every robot that is currently in the world
        for (var robot : world.getAllRobots()) {
            System.out.println("  Disconnecting robot: " + robot.getName());
        }
        world.removeAllRobots();

        System.out.println("All robots disconnected. Goodbye!");

        // Exit the whole server process
        System.exit(0);
    }
}