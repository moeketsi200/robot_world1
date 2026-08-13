package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

// Tests for ServerQuitCommand.
// We can't test System.exit() directly, so we test that it clears the robots
// before the exit would happen.
class ServerQuitCommandTest {

    private World world;

    @BeforeEach
    void setUp() {
        world = new World(20, 20);
        world.getObstacles().clear();
    }

    @Test
    void removeAllRobotsEmptiesTheWorld() {
        // Add two robots to the world
        world.addRobot(new Robot("Hal",  "Scout", 3, 3));
        world.addRobot(new Robot("Eve",  "Tank",  5, 2));

        assertEquals(2, world.getAllRobots().size());

        // removeAllRobots is what ServerQuitCommand calls before System.exit()
        world.removeAllRobots();

        assertTrue(world.getAllRobots().isEmpty());
    }

    @Test
    void removeAllRobotsOnEmptyWorldDoesNotCrash() {
        // Should work fine even if there are no robots
        world.removeAllRobots();
        assertTrue(world.getAllRobots().isEmpty());
    }

    @Test
    void removeAllRobotsRemovesEveryRobot() {
        world.addRobot(new Robot("A", "Scout", 3, 3));
        world.addRobot(new Robot("B", "Scout", 3, 3));
        world.addRobot(new Robot("C", "Scout", 3, 3));

        world.removeAllRobots();

        // None of the robots should still be in the world
        assertNull(world.getRobot("A"));
        assertNull(world.getRobot("B"));
        assertNull(world.getRobot("C"));
    }
}
