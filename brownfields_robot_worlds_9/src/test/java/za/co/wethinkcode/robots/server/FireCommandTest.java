package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.FireCommand;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class FireCommandTest {

    @Test
    void shouldReturnErrorIfRobotDoesNotExist() {
            World world = new World(21, 0);

    world.clearForTesting();

        String result = new FireCommand("Ghost").execute(world);

        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void shouldReturnErrorWhenNoAmmo() {
        World world = new World(21, 0);
        world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);

        // 1. Add to world first (world forces ammo to 5)
        world.addRobot(shooter);

        // 2. NOW drain the ammo to 0
        shooter.setShots(0);

        String result = new FireCommand("Shooter").execute(world);

        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void shouldConsumeAmmoWhenFiring() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        world.addRobot(shooter);

        shooter.setPosition(new Position(5, 10));

        int before = shooter.getShots();

        new FireCommand("Shooter").execute(world);

        assertEquals(before - 1, shooter.getShots());
    }

    @Test
    void shouldMissWhenNoTargetInRange() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        world.addRobot(shooter);

        shooter.setPosition(new Position(5, 10));

        String result = new FireCommand("Shooter").execute(world);

        assertEquals("MISS", result);
    }

    @Test
    void shouldHitTargetInRange() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        Robot target = new Robot("Target", "Tank", 5, 3);

        world.addRobot(shooter);
        world.addRobot(target);

        shooter.setPosition(new Position(5, 10));
        target.setPosition(new Position(5, 8));

        String result = new FireCommand("Shooter").execute(world);

        assertTrue(result.startsWith("HIT"));
    }

    @Test
    void shouldReduceTargetShieldWhenHit() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        Robot target = new Robot("Target", "Tank", 5, 3);

        world.addRobot(shooter);
        world.addRobot(target);

        shooter.setPosition(new Position(5, 10));
        target.setPosition(new Position(5, 8));

        new FireCommand("Shooter").execute(world);

        assertEquals(4, target.getShields());
    }

    @Test
    void shouldKillRobotWithOneShield() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        Robot target = new Robot("Target", "Tank", 1, 3);

        world.addRobot(shooter);
        world.addRobot(target);

        target.setShields(1);

        shooter.setPosition(new Position(5, 10));
        target.setPosition(new Position(5, 8));

        String result = new FireCommand("Shooter").execute(world);

        assertFalse(target.isAlive());
        assertNull(world.getRobot("Target"));
        assertTrue(result.contains("KILLED"));
    }

    @Test
    void shouldNotHitTargetOutsideRange() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        Robot target = new Robot("FarBot", "Scout", 3, 3);

        world.addRobot(shooter);
        world.addRobot(target);

        shooter.setPosition(new Position(5, 10));
        target.setPosition(new Position(5, 6));

        String result = new FireCommand("Shooter").execute(world);

        assertEquals("MISS", result);

    }

    @Test
    void shouldHitFirstRobotInLine() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        Robot first = new Robot("First", "Tank", 5, 3);
        Robot second = new Robot("Second", "Tank", 5, 3);

        world.addRobot(shooter);
        world.addRobot(first);
        world.addRobot(second);

        shooter.setPosition(new Position(5, 10));
        first.setPosition(new Position(5, 9));
        second.setPosition(new Position(5, 8));

        new FireCommand("Shooter").execute(world);

        assertEquals(4, first.getShields());
        assertEquals(5, second.getShields());
    }

    @Test
    void shouldRunOutOfAmmoAfterMultipleShots() {
            World world = new World(21, 0);

    world.clearForTesting();

        Robot shooter = new Robot("Shooter", "Scout", 3, 3);
        world.addRobot(shooter);

        shooter.setPosition(new Position(5, 10));

        while (shooter.getShots() > 0) {
            new FireCommand("Shooter").execute(world);
        }

        assertEquals(0, shooter.getShots());

        String result = new FireCommand("Shooter").execute(world);

        assertTrue(result.startsWith("ERROR"));
    }
}
