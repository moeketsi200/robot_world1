package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.commands.TurnCommand;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class TurnCommandTest {

    private World world;
    private Robot robot;

    @BeforeEach
    void setUp() {
        world = new World(21, 0);
        robot = new Robot("R1", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setPosition(new Position(5, 5));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Pre-rotate the robot to a desired facing direction before the test turn. */
    private void faceRobot(Direction target) {
        // Start from NORTH (default) and keep turning right until we reach target.
        while (robot.getDirection() != target) {
            robot.turnRight();
        }
    }

    /** Assert that a turn with the given direction is blocked with the given error keyword. */
    private void assertTurnBlocked(Status status, String direction, String expectedKeyword) {
        robot.setStatus(status);
        String result = new TurnCommand("R1", direction).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains(expectedKeyword));
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test
    void turnReturnsErrorWhenRobotNotFound() {
        String result = new TurnCommand("ghost", "left").execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void turnReturnsErrorForInvalidDirection() {
        String result = new TurnCommand("R1", "up").execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void turnBlockedWhenRepairing() {
        assertTurnBlocked(Status.REPAIR, "left", "repairing");
    }

    @Test
    void turnBlockedWhenReloading() {
        assertTurnBlocked(Status.RELOAD, "left", "reloading");
    }

    // ── Turn left ─────────────────────────────────────────────────────────────

    @Test
    void turnLeftFromNorthFacesWest() {
        new TurnCommand("R1", "left").execute(world);
        assertEquals(Direction.WEST, robot.getDirection());
    }

    @Test
    void turnLeftFromWestFacesSouth() {
        faceRobot(Direction.WEST);
        new TurnCommand("R1", "left").execute(world);
        assertEquals(Direction.SOUTH, robot.getDirection());
    }

    @Test
    void turnLeftFromSouthFacesEast() {
        faceRobot(Direction.SOUTH);
        new TurnCommand("R1", "left").execute(world);
        assertEquals(Direction.EAST, robot.getDirection());
    }

    @Test
    void turnLeftFromEastFacesNorth() {
        faceRobot(Direction.EAST);
        new TurnCommand("R1", "left").execute(world);
        assertEquals(Direction.NORTH, robot.getDirection());
    }

    // ── Turn right ────────────────────────────────────────────────────────────

    @Test
    void turnRightFromNorthFacesEast() {
        new TurnCommand("R1", "right").execute(world);
        assertEquals(Direction.EAST, robot.getDirection());
    }

    @Test
    void turnRightFromEastFacesSouth() {
        faceRobot(Direction.EAST);
        new TurnCommand("R1", "right").execute(world);
        assertEquals(Direction.SOUTH, robot.getDirection());
    }

    @Test
    void turnRightFromSouthFacesWest() {
        faceRobot(Direction.SOUTH);
        new TurnCommand("R1", "right").execute(world);
        assertEquals(Direction.WEST, robot.getDirection());
    }

    @Test
    void turnRightFromWestFacesNorth() {
        faceRobot(Direction.WEST);
        new TurnCommand("R1", "right").execute(world);
        assertEquals(Direction.NORTH, robot.getDirection());
    }

    // ── Return value ──────────────────────────────────────────────────────────

    @Test
    void turnReturnsDoneWithNewDirection() {
        String result = new TurnCommand("R1", "right").execute(world);
        assertTrue(result.startsWith("Done"));
        assertTrue(result.contains("EAST"));
    }

    @Test
    void turnIsCaseInsensitive() {
        String result = new TurnCommand("R1", "LEFT").execute(world);
        assertTrue(result.startsWith("Done"));
        assertEquals(Direction.WEST, robot.getDirection());
    }

    // ── Full rotation ─────────────────────────────────────────────────────────

    @Test
    void fourLeftTurnsReturnToNorth() {
        for (int i = 0; i < 4; i++) new TurnCommand("R1", "left").execute(world);
        assertEquals(Direction.NORTH, robot.getDirection());
    }

    @Test
    void fourRightTurnsReturnToNorth() {
        for (int i = 0; i < 4; i++) new TurnCommand("R1", "right").execute(world);
        assertEquals(Direction.NORTH, robot.getDirection());
    }

    // ── Position unchanged ────────────────────────────────────────────────────

    @Test
    void turnDoesNotChangePosition() {
        Position before = robot.getPosition();
        new TurnCommand("R1", "right").execute(world);
        assertEquals(before.getX(), robot.getPosition().getX());
        assertEquals(before.getY(), robot.getPosition().getY());
    }
}