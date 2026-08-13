package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.BackCommand;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class BackCommandTest {

    //Error cases

    @Test
    void backReturnsErrorWhenRobotNotFound() {
        World world = new World(21, 0);
        String result = new BackCommand("ghost", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void backBlockedWhenRepairing() {
        World world = new World(21, 0);
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.REPAIR);

        String result = new BackCommand("R1", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("repairing"));
    }

    @Test
    void backBlockedWhenReloading() {
        World world = new World(21, 0);
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.RELOAD);

        String result = new BackCommand("R1", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("reloading"));
    }

    //Basic movement

    @Test
    void backReturnsDoneWhenMoveSucceeds() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));

        String result = new BackCommand("R1", 1).execute(world);
        assertEquals("Done", result);
    }

    @Test
    void backMovesOppositeToFacingNorth() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(10, 10));
        // Facing NORTH — back moves SOUTH (y increases)

        int yBefore = robot.getPosition().getY();
        new BackCommand("R1", 1).execute(world);

        assertEquals(10, robot.getPosition().getX());
        assertEquals(yBefore + 1, robot.getPosition().getY());
    }

    @Test
    void backMovesOppositeToFacingSouth() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
        robot.turnRight();
        robot.turnRight(); // now facing SOUTH — back moves NORTH (y decreases)

        assertEquals(Direction.SOUTH, robot.getDirection());
        new BackCommand("R1", 1).execute(world);

        assertEquals(5, robot.getPosition().getX());
        assertEquals(4, robot.getPosition().getY());
    }

    @Test
    void backMovesOppositeToFacingEast() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
        robot.turnRight(); // now facing EAST — back moves WEST (x decreases)

        assertEquals(Direction.EAST, robot.getDirection());
        new BackCommand("R1", 1).execute(world);

        assertEquals(4, robot.getPosition().getX());
        assertEquals(5, robot.getPosition().getY());
    }

    @Test
    void backMovesOppositeToFacingWest() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
        robot.turnLeft(); // now facing WEST — back moves EAST (x increases)

        assertEquals(Direction.WEST, robot.getDirection());
        new BackCommand("R1", 1).execute(world);

        assertEquals(6, robot.getPosition().getX());
        assertEquals(5, robot.getPosition().getY());
    }

    @Test
    void backMovesMultipleSteps() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(10, 10));
        // Facing NORTH — back moves south (y increases)

        int yBefore = robot.getPosition().getY();
        new BackCommand("R1", 3).execute(world);

        // Robot should have moved at least 1 step south
        assertTrue(robot.getPosition().getY() > yBefore);
    }

    //Obstruction

    @Test
    void backReturnsObstructedAtWorldBoundary() {
        World world = new World(21, 0);
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        // Facing NORTH at bottom edge — back would go further south, out of bounds
        robot.setPosition(new Position(5, 20));

        String result = new BackCommand("R1", 1).execute(world);
        assertEquals("Obstructed", result);
    }

    @Test
    void backDoesNotMoveRobotWhenFullyObstructed() {
        World world = new World(21, 0);
        Robot robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 20)); // at bottom boundary facing NORTH

        Position before = robot.getPosition();
        new BackCommand("R1", 1).execute(world);

        assertEquals(before.getX(), robot.getPosition().getX());
        assertEquals(before.getY(), robot.getPosition().getY());
    }

    @Test
    void backBlockedByAnotherRobot() {
        World world = new World(21, 0);
        world.clearForTesting();
        Robot robot1 = new Robot("R1", "Scout", 5, 3);
        Robot robot2 = new Robot("R2", "Scout", 5, 3);
        world.addRobot(robot1);
        world.addRobot(robot2);

        // R1 facing NORTH at (5,5) — back goes south to (5,6)
        // Place R2 at (5,6) to block
        robot1.setPosition(new Position(5, 5));
        robot2.setPosition(new Position(5, 6));

        String result = new BackCommand("R1", 1).execute(world);
        assertEquals("Obstructed", result);
        assertEquals(5, robot1.getPosition().getX());
        assertEquals(5, robot1.getPosition().getY());
    }
}
