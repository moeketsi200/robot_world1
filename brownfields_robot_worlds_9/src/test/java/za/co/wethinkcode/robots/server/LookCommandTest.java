package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.commands.LookCommand;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import za.co.wethinkcode.robots.server.Robot;

class LookCommandTest {

    private World world;
    private Robot testRobot;

    @BeforeEach
    void setUp() {
        // Create a 20x20 world and clear any random obstacles
        world = new World(20, 20);
        world.clearForTesting(); // TRAP 1 DEAD: Wipe the walls!

        // Give Hal normal stats (5 shields, 5 sight distance)
        testRobot = new Robot("Hal", "Scout", 5, 5);
        world.addRobot(testRobot);

        // TRAP 2 DEAD: Force the position AFTER the world drops him randomly
        testRobot.setPosition(new Position(10, 10));
    }

    @Test
    void executeReturnsErrorWhenRobotNotFound() {
        World world = new World(20, 20);
        world.clearForTesting();

        LookCommand command = new LookCommand("Ghost");
        String result = command.execute(world);

        assertEquals("ERROR: Robot not found", result);
    }

    @Test
    void executeSeesNothingInEmptyWorld() {
        LookCommand command = new LookCommand("Hal");
        String result = command.execute(world);

        String expected = "NORTH: Nothing visible\n" +
                "SOUTH: Nothing visible\n" +
                "EAST: Nothing visible\n" +
                "WEST: Nothing visible";

        assertEquals(expected, result);
    }

    @Test
    void executeDetectsWorldEdges() {
        // TRAP 3 DEAD: Give EdgeRover 5 sight distance so he isn't blind!
        Robot cornerRobot = new Robot("EdgeRover", "Scout", 5, 5);
        world.addRobot(cornerRobot);

        // Force him into the very top-left corner
        cornerRobot.setPosition(new Position(0, 0));

        LookCommand command = new LookCommand("EdgeRover");
        String result = command.execute(world);

        String expected = "NORTH: Edge at distance 1\n" +
                "SOUTH: Nothing visible\n" +
                "EAST: Nothing visible\n" +
                "WEST: Edge at distance 1";

        assertEquals(expected, result);
    }

    @Test
    void executeDetectsOtherRobots() {
        // Give Eve normal stats
        Robot targetRobot = new Robot("Eve", "Tank", 5, 5);
        world.addRobot(targetRobot);

        // Force Eve to be exactly 2 steps SOUTH of Hal
        targetRobot.setPosition(new Position(10, 12));

        LookCommand command = new LookCommand("Hal");
        String result = command.execute(world);

        // It should see Eve to the SOUTH at distance 2
        String expected = "NORTH: Nothing visible\n" +
                "SOUTH: Robot Eve at distance 2\n" +
                "EAST: Nothing visible\n" +
                "WEST: Nothing visible";

        assertEquals(expected, result);
    }
}