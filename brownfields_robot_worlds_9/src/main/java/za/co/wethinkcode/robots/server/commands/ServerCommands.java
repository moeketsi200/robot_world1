package za.co.wethinkcode.robots.server.commands;

import java.util.Scanner;

import za.co.wethinkcode.robots.server.world.World;

public class ServerCommands implements Runnable {

    private World world;

    public ServerCommands(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()) {
                System.out.print("Enter server command: ");
                String input = scanner.nextLine();

                if (input.equals("quit")) {
                    new ServerQuitCommand().execute(world);
                } else if (input.equals("robots")) {
                    new RobotsCommand().execute(world);
                } else if (input.equals("dump")) {
                    new DumpCommand().execute(world);
                } else if (input.equals("purge")) {
                    new PurgeCommand().purgeRobots(world);
                } else {
                    System.out.println("Please enter valid command (quit, robots, dump, purge)");
                }
            }
        } catch (java.util.NoSuchElementException e) {
            // stdin closed (e.g. running in background) — exit the console thread silently
        }
    }
}
