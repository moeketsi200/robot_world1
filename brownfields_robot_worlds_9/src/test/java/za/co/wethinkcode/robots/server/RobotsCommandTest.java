package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.RobotsCommand;
import za.co.wethinkcode.robots.server.world.World;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RobotsCommandTest {

    // Variables to hijack the terminal output
    private final ByteArrayOutputStream terminalOutput = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        // Reroute System.out to our terminalOutput stream before each test
        System.setOut(new PrintStream(terminalOutput));
    }

    @AfterEach
    void restoreStreams() {
        // Put System.out back to normal so we don't break the rest of the project
        System.setOut(originalOut);
    }

    @Test
    void executePrintsEmptyMessageWhenNoRobots() {
        World world = new World(20, 0);
        world.clearForTesting(); // Give us a blank slate

        RobotsCommand command = new RobotsCommand();
        command.execute(world);

        String output = terminalOutput.toString();

        // Check that it noticed the world was empty
        assertTrue(output.contains("=== Robots in the world ==="));
        assertTrue(output.contains("(no robots connected)"));
    }

    @Test
    void executePrintsRobotDetailsWhenRobotsExist() {
        World world = new World(20, 0);
        world.clearForTesting();

        // Add a single robot to the world
        Robot robot = new Robot("TestBot", "Scout", 5, 5);
        world.addRobot(robot);

        RobotsCommand command = new RobotsCommand();
        command.execute(world);

        String output = terminalOutput.toString();

        // Verify the loop ran and printed our robot's details
        assertTrue(output.contains("=== Robots in the world ==="));
        assertTrue(output.contains("Name     : TestBot"));
        assertTrue(output.contains("Make     : Scout"));

        // Verify it printed the correct final count
        assertTrue(output.contains("Total: 1 robot(s)"));
    }
}