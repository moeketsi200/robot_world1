package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.ClientQuitCommand;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class ClientQuitCommandTest {

    @Test
    void quitReturnsGoodbye() {
        World world = new World(21, 0, 3, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);

        String result = new ClientQuitCommand
("HAL").execute(world);
        assertEquals("Goodbye", result);
    }

    @Test
    void quitRemovesRobotFromWorld() {
        World world = new World(21, 0, 3, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);

        assertNotNull(world.getRobot("HAL"));
        new ClientQuitCommand
("HAL").execute(world);
        assertNull(world.getRobot("HAL"));
    }

    @Test
    void quitForUnknownRobotDoesNotThrow() {
        World world = new World(21, 0, 3, 5);
        assertDoesNotThrow(() -> new ClientQuitCommand
("ghost").execute(world));
    }

    @Test
    void quitDoesNotAffectOtherRobots() {
        World world = new World(21, 0, 3, 5);
        Robot hal  = new Robot("HAL",  "Scout", 5, 3);
        Robot wall = new Robot("WALL", "Scout", 5, 3);
        world.addRobot(hal);
        world.addRobot(wall);

        new ClientQuitCommand
("HAL").execute(world);

        assertNull(world.getRobot("HAL"));
        assertNotNull(world.getRobot("WALL"));
    }

    @Test
    void quitCanBeCalledOnlyOnce() {
        World world = new World(21, 0, 3, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);

        new ClientQuitCommand
("HAL").execute(world);
        // Second quit on same name should not throw
        assertDoesNotThrow(() -> new ClientQuitCommand
("HAL").execute(world));
        assertNull(world.getRobot("HAL"));
    }
}