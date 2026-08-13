package za.co.wethinkcode.robots.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.commands.LaunchCommand;
import za.co.wethinkcode.robots.server.commands.StateCommand;
import za.co.wethinkcode.robots.server.world.World;

// Tests for LaunchCommand
class LaunchCommandTest {

    private World world;

    @BeforeEach
    void setUp() {
        world = new World(20, 20);
        world.getObstacles().clear();
    }

    @Test
    void successfulLaunchReturnsOK() {
        LaunchCommand cmd = new LaunchCommand("Hal", "Scout", 3, 3);
        String result = cmd.execute(world);

        assertTrue(result.startsWith("OK"));
        assertNotNull(world.getRobot("Hal"));
    }

    @Test
    void launchingDuplicateNameReturnsError() {
        new LaunchCommand("Hal", "Scout", 3, 3).execute(world);
        String result = new LaunchCommand("Hal", "Tank", 5, 2).execute(world);

        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("Too many"));
    }

    @Test 
    void launchedRobotFacesNorth() {
        new LaunchCommand("Bot", "Scout", 3, 3).execute(world);

        String result = new StateCommand("Bot").execute(world);
        assertTrue(result.contains("NORTH"));
    }

    @Test
    void launchedRobotNeverLandsOnObstacle() {
        za.co.wethinkcode.robots.server.world.Position obstaclePos = new za.co.wethinkcode.robots.server.world.Position(1, 1);
        world.getObstacles().add(obstaclePos);

        for (int i = 1; i <= 8; i++) {
            new LaunchCommand("Bot" + i, "Scout", 3, 3).execute(world);
            za.co.wethinkcode.robots.server.Robot robot = world.getRobot("Bot" + i);
            assertNotNull(robot);
            org.junit.jupiter.api.Assertions.assertFalse(
                robot.getPosition().getX() == 1 && robot.getPosition().getY() == 1,
                "Robot Bot" + i + " should not be placed on obstacle position [1, 1]"
            );
        }
    }
}
