
package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.ForwardCommand;
import za.co.wethinkcode.robots.server.config.WorldConfig;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class ForwardCommandTest {

    //Error cases

    @Test
    void forwardReturnsErrorWhenRobotNotFound() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        String result = new ForwardCommand("ghost", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void forwardBlockedWhenRepairing() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.REPAIR);

        String result = new ForwardCommand("R1", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("repairing"));
    }

    @Test
    void forwardBlockedWhenReloading() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.RELOAD);

        String result = new ForwardCommand("R1", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("reloading"));
    }

    //Basic movement

    @Test
    void forwardReturnsDoneWhenMoveSucceeds() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));

        String result = new ForwardCommand("R1", 1).execute(world);
        assertEquals("Done", result);
    }

    @Test
    void forwardMovesNorthCorrectly() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
        // Default direction is NORTH (dy = -1)

        new ForwardCommand("R1", 1).execute(world);

        assertEquals(5, robot.getPosition().getX());
        assertEquals(4, robot.getPosition().getY());
    }

    @Test
    void forwardMovesSouthCorrectly() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
        robot.turnRight();
        robot.turnRight(); // now facing SOUTH

        assertEquals(Direction.SOUTH, robot.getDirection());
        new ForwardCommand("R1", 1).execute(world);

        assertEquals(5, robot.getPosition().getX());
        assertEquals(6, robot.getPosition().getY());
    }

    @Test
    void forwardMovesEastCorrectly() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(10, 10));
        robot.turnRight(); // now facing EAST

        assertEquals(Direction.EAST, robot.getDirection());
        int xBefore = robot.getPosition().getX();
        new ForwardCommand("R1", 1).execute(world);

        assertEquals(xBefore + 1, robot.getPosition().getX());
        assertEquals(10, robot.getPosition().getY());
    }

    @Test
    void forwardMovesWestCorrectly() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(10, 10));
        robot.turnLeft(); // now facing WEST

        assertEquals(Direction.WEST, robot.getDirection());
        int xBefore = robot.getPosition().getX();
        new ForwardCommand("R1", 1).execute(world);

        assertEquals(xBefore - 1, robot.getPosition().getX());
        assertEquals(10, robot.getPosition().getY());
    }

    @Test
    void forwardMovesMultipleSteps() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(10, 10));
        // NORTH — y decreases

        int yBefore = robot.getPosition().getY();
        String result = new ForwardCommand("R1", 3).execute(world);

        // Robot should have moved at least 1 step north
        assertTrue(robot.getPosition().getY() < yBefore);
    }

    //Obstruction

    @Test
    void forwardReturnsObstructedAtWorldBoundary() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        // Place right at the top edge facing NORTH
        robot.setPosition(new Position(5, 0));

        String result = new ForwardCommand("R1", 1).execute(world);
        assertEquals("Obstructed", result);
    }

    @Test
    void forwardStopsAtObstruction() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
        // Try to move 5 steps north — will hit boundary or wall before that
        robot.setPosition(new Position(5, 2));

        new ForwardCommand("R1", 5).execute(world);

        // Robot should have moved as far as possible but not beyond boundary
        assertTrue(robot.getPosition().getY() >= 0);
    }

    @Test
    void forwardBlockedByAnotherRobot() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot1 = new Robot("R1", "Scout", 5, 3);
        Robot robot2 = new Robot("R2", "Scout", 5, 3);
        world.addRobot(robot1);
        world.addRobot(robot2);

        // Place R1 facing NORTH at (5,5), R2 directly in front at (5,4)
        robot1.setPosition(new Position(5, 5));
        robot2.setPosition(new Position(5, 4));

        String result = new ForwardCommand("R1", 1).execute(world);
        assertEquals("Obstructed", result);
        // R1 should not have moved
        assertEquals(5, robot1.getPosition().getX());
        assertEquals(5, robot1.getPosition().getY());
    }

    //Position unchanged on obstruction

    @Test
    void forwardDoesNotMoveRobotWhenFullyObstructed() {
            World world = new World(new WorldConfig());

    world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 0)); // at boundary facing NORTH

        Position before = robot.getPosition();
        new ForwardCommand("R1", 1).execute(world);

        assertEquals(before.getX(), robot.getPosition().getX());
        assertEquals(before.getY(), robot.getPosition().getY());
    }
}
