package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.commands.DumpCommand;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.World;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DumpCommandTest {

    // These variables let us hijack the terminal output
    private final ByteArrayOutputStream terminalOutput = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        // Reroute System.out to our terminalOutput stream before each test
        System.setOut(new PrintStream(terminalOutput));
    }

    @AfterEach
    void restoreStreams() {
        // Put System.out back to normal so we don't break other tests!
        System.setOut(originalOut);
    }

    @Test
    void executePrintsEmptyWorldCorrectly() {
        World world = new World(20, 0);
        world.clearForTesting(); // Give us a blank slate

        DumpCommand command = new DumpCommand();
        command.execute(world);

        String output = terminalOutput.toString();

        // Check the headers
        assertTrue(output.contains("WORLD DUMP"));
        assertTrue(output.contains("World settings:"));

        // Since we cleared the world, it should explicitly say "(none)"
        assertTrue(output.contains("(none)"));
    }

    @Test
    void executePrintsPopulatedWorldCorrectly() {
        World world = new World(20, 0);
        world.clearForTesting();

        // 1. Manually add an obstacle
        world.getObstacles().add(new Position(5, 5));

        // 2. Manually add a robot
        Robot robot = new Robot("DumpBot", "Scout", 5, 5);
        world.addRobot(robot);

        DumpCommand command = new DumpCommand();
        command.execute(world);

        String output = terminalOutput.toString();

        // We don't need to check the exact formatting of the whole string,
        // we just need to ensure the data was printed!
        assertTrue(output.contains("(5,5)"));
        assertTrue(output.contains("DumpBot"));
    }
}