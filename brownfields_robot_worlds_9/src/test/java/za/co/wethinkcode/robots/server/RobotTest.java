package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Status;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {

    @Test
    void shouldCreateRobotWithDefaultValues() {
        Robot robot = new Robot("HAL");

        assertEquals("HAL", robot.getName());
        assertEquals(new Position(0, 0), robot.getPosition());
        assertEquals(Direction.NORTH, robot.getDirection());
        assertEquals(Status.NORMAL, robot.getStatus());
        assertEquals(3, robot.getShields());
        assertEquals(3, robot.getMaxShields());
        assertEquals("Default", robot.getKind());
        assertEquals(3, robot.getShotDistance());
        assertEquals(3, robot.getShots());
        assertEquals(3, robot.getMaxShots());
    }

    @Test
    void shouldCreateRobotWithCustomValues() {
        Robot robot = new Robot("EVE", "Scout", 5, 2);

        assertEquals("EVE", robot.getName());
        assertEquals("Scout", robot.getKind());
        assertEquals(5, robot.getShields());
        assertEquals(5, robot.getMaxShields());
        assertEquals(2, robot.getShotDistance());
        assertEquals(4, robot.getShots());
        assertEquals(4, robot.getMaxShots());
        assertEquals(Direction.NORTH, robot.getDirection());
        assertEquals(Status.NORMAL, robot.getStatus());
    }

    @Test
    void shouldTurnLeftFromNorthToWest() {
        Robot robot = new Robot("HAL");

        robot.turnLeft();

        assertEquals(Direction.WEST, robot.getDirection());
    }

    @Test
    void shouldTurnRightFromNorthToEast() {
        Robot robot = new Robot("HAL");

        robot.turnRight();

        assertEquals(Direction.EAST, robot.getDirection());
    }

    @Test
    void shouldReturnToNorthAfterFourLeftTurns() {
        Robot robot = new Robot("HAL");

        robot.turnLeft();
        robot.turnLeft();
        robot.turnLeft();
        robot.turnLeft();

        assertEquals(Direction.NORTH, robot.getDirection());
    }

    @Test
    void shouldReturnToNorthAfterFourRightTurns() {
        Robot robot = new Robot("HAL");

        robot.turnRight();
        robot.turnRight();
        robot.turnRight();
        robot.turnRight();

        assertEquals(Direction.NORTH, robot.getDirection());
    }

    @Test
    void shouldSetPosition() {
        Robot robot = new Robot("HAL");
        Position newPosition = new Position(3, 4);

        robot.setPosition(newPosition);

        assertEquals(newPosition, robot.getPosition());
    }

    @Test
    void shouldReduceShieldWhenTakingDamage() {
        Robot robot = new Robot("HAL");

        robot.takeDamage(1);

        assertEquals(2, robot.getShields());
        assertEquals(Status.NORMAL, robot.getStatus());
    }

    @Test
    void shouldDieWhenShieldsReachZero() {
        Robot robot = new Robot("HAL");

        robot.takeDamage(3);

        assertEquals(0, robot.getShields());
        assertEquals(Status.DEAD, robot.getStatus());
        assertFalse(robot.isAlive());
    }

    @Test
    void shouldNotAllowShieldsBelowZero() {
        Robot robot = new Robot("HAL");

        robot.takeDamage(10);

        assertEquals(0, robot.getShields());
        assertEquals(Status.DEAD, robot.getStatus());
    }

    @Test
    void shouldRepairShieldsToMaximum() {
        Robot robot = new Robot("HAL", "Scout", 5, 2);

        robot.takeDamage(3);
        robot.repairShields();

        assertEquals(5, robot.getShields());
    }

    @Test
    void shouldReduceShots() {
        Robot robot = new Robot("HAL");

        robot.reduceShots(1);

        assertEquals(2, robot.getShots());
    }

    @Test
    void shouldNotAllowShotsBelowZero() {
        Robot robot = new Robot("HAL");

        robot.reduceShots(10);

        assertEquals(0, robot.getShots());
    }

    @Test
    void shouldReloadShotsToMaximum() {
        Robot robot = new Robot("HAL", "Scout", 3, 2);

        robot.reduceShots(2);
        robot.reloadShots();

        assertEquals(robot.getMaxShots(), robot.getShots());
    }

    @Test
    void shouldSetShotsDirectly() {
        Robot robot = new Robot("HAL");

        robot.setShots(1);

        assertEquals(1, robot.getShots());
    }

    @Test
    void shouldHaveZeroShotsWhenShotDistanceIsZero() {
        Robot robot = new Robot("HAL", "Tank", 3, 0);

        assertEquals(0, robot.getShots());
        assertEquals(0, robot.getMaxShots());
    }

    @Test
    void toStringShouldContainImportantState() {
        Robot robot = new Robot("HAL");

        String result = robot.toString();

        assertTrue(result.contains("HAL"));
        assertTrue(result.contains("facing"));
        assertTrue(result.contains("shields="));
        assertTrue(result.contains("shots="));
        assertTrue(result.contains("status="));
    }
}
