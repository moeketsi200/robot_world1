package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Robot Tests")
public class RobotTest {

    @Test
    @DisplayName("Constructor sets fields and defaults correctly")
    public void testConstructorAndGetters() {
        Robot r = new Robot("R1", "heavy", 3, 7);
        assertEquals("R1", r.getName());
        assertEquals("heavy", r.getKind());
        assertEquals(0, r.getX());
        assertEquals(0, r.getY());
        assertEquals("NORTH", r.getDirection());
        assertEquals(3, r.getShields());
        assertEquals(7, r.getShots());
        assertEquals("NORMAL", r.getStatus());
    }

    @Test
    @DisplayName("setPosition updates coordinates")
    public void testSetPosition() {
        Robot r = new Robot("R2", "normal", 5, 5);
        r.setPosition(10, 20);
        assertEquals(10, r.getX());
        assertEquals(20, r.getY());
    }

    @Test
    @DisplayName("toString contains key information")
    public void testToStringContainsFields() {
        Robot r = new Robot("Alpha", "scout", 2, 9);
        r.setPosition(3,4);
        String s = r.toString();
        assertTrue(s.contains("Alpha"));
        assertTrue(s.contains("scout"));
        assertTrue(s.contains("Position: [3, 4]"));
        assertTrue(s.contains("Shields: 2"));
        assertTrue(s.contains("Ammo: 9 shot(s)"));
        assertTrue(s.contains("Status: NORMAL"));
    }

    @Test
    @DisplayName("updateDirection turns the robot correctly")
    public void testUpdateDirection() {
        Robot r = new Robot("R1", "normal", 5, 5);
        assertEquals("NORTH", r.getDirection());

        r.updateDirection(true); // turn right
        assertEquals("EAST", r.getDirection());

        r.updateDirection(true); // turn right
        assertEquals("SOUTH", r.getDirection());

        r.updateDirection(false); // turn left
        assertEquals("EAST", r.getDirection());

        r.updateDirection(false); // turn left
        assertEquals("NORTH", r.getDirection());

        r.updateDirection(false); // turn left
        assertEquals("WEST", r.getDirection());
    }

    @Test
    @DisplayName("updatePosition moves robot in open world")
    public void testUpdatePosition() {
        World w = new World(200, 200);
        Robot r = new Robot("R1", "normal", 5, 5);
        r.setPosition(0, 0);

        // Move North (Y increases)
        String res = r.updatePosition(10, w);
        assertEquals("OK", res);
        assertEquals(0, r.getX());
        assertEquals(10, r.getY());

        // Move Backwards (South)
        res = r.updatePosition(-5, w);
        assertEquals("OK", res);
        assertEquals(0, r.getX());
        assertEquals(5, r.getY());

        r.updateDirection(true); // Facing EAST (X increases)
        res = r.updatePosition(10, w);
        assertEquals("OK", res);
        assertEquals(10, r.getX());
        assertEquals(5, r.getY());
    }

    @Test
    @DisplayName("updatePosition stops at edges")
    public void testUpdatePositionEdge() {
        World w = new World(200, 200);
        Robot r = new Robot("R1", "normal", 5, 5);
        r.setPosition(95, 0);

        r.updateDirection(true); // Facing EAST
        String res = r.updatePosition(10, w); // Try to move to 105
        assertEquals("Obstructed", res);
        assertEquals(100, r.getX()); // Last valid position is 100
        assertEquals(0, r.getY());
    }

    @Test
    @DisplayName("updatePosition stops at obstacles")
    public void testUpdatePositionObstacle() {
        World w = new World(200, 200);
        w.getObjects().add(new WorldObject(0, 5, "rock"));

        Robot r = new Robot("R1", "normal", 5, 5);
        r.setPosition(0, 0);

        // Facing NORTH, obstacle at 5
        String res = r.updatePosition(10, w);
        assertEquals("Obstructed", res);
        assertEquals(0, r.getX());
        assertEquals(4, r.getY()); // Last valid position is 4
    }

    @Test
    @DisplayName("updatePosition stops at other robots")
    public void testUpdatePositionRobot() {
        World w = new World(200, 200);
        Robot other = new Robot("R2", "normal", 5, 5);
        other.setPosition(0, 5);
        w.addRobot(other);

        Robot r = new Robot("R1", "normal", 5, 5);
        r.setPosition(0, 0);
        w.addRobot(r);

        // Facing NORTH, other robot at 5
        String res = r.updatePosition(10, w);
        assertEquals("Obstructed", res);
        assertEquals(0, r.getX());
        assertEquals(4, r.getY()); // Last valid position is 4
    }

    @Test
    @DisplayName("takeDamage decreases shields and marks robot dead when shields reach 0")
    public void testTakeDamage() {
        Robot r = new Robot("R1", "normal", 3, 5);
        assertEquals(3, r.getShields());
        assertEquals("NORMAL", r.getStatus());
        assertTrue(r.isAlive());

        // First hit
        r.takeDamage();
        assertEquals(2, r.getShields());
        assertEquals("NORMAL", r.getStatus());
        assertTrue(r.isAlive());

        // Second hit
        r.takeDamage();
        assertEquals(1, r.getShields());
        assertEquals("NORMAL", r.getStatus());
        assertTrue(r.isAlive());

        // Third hit - should die
        r.takeDamage();
        assertEquals(0, r.getShields());
        assertEquals("DEAD", r.getStatus());
        assertFalse(r.isAlive());

        // Additional hits shouldn't go negative
        r.takeDamage();
        assertEquals(0, r.getShields());
        assertEquals("DEAD", r.getStatus());
        assertFalse(r.isAlive());
    }

    @Test
    @DisplayName("takeDamage with amount parameter decreases shields by specified amount")
    public void testTakeDamageWithAmount() {
        Robot r = new Robot("R1", "heavy", 5, 5);
        assertEquals(5, r.getShields());
        assertEquals("NORMAL", r.getStatus());

        // Take 2 damage
        r.takeDamage(2);
        assertEquals(3, r.getShields());
        assertEquals("NORMAL", r.getStatus());
        assertTrue(r.isAlive());

        // Take 5 damage (more than current shields)
        r.takeDamage(5);
        assertEquals(0, r.getShields());
        assertEquals("DEAD", r.getStatus());
        assertFalse(r.isAlive());
    }

    @Test
    @DisplayName("restoreShields increases shield value")
    public void testRestoreShields() {
        Robot r = new Robot("R1", "normal", 3, 5);
        r.takeDamage();
        r.takeDamage();
        assertEquals(1, r.getShields());

        r.restoreShields(2);
        assertEquals(3, r.getShields());
        assertTrue(r.isAlive());
    }

    @Test
    @DisplayName("setShields changes shield value and can resurrect dead robot")
    public void testSetShields() {
        Robot r = new Robot("R1", "normal", 3, 5);
        
        // Take damage until dead
        r.takeDamage(3);
        assertEquals(0, r.getShields());
        assertEquals("DEAD", r.getStatus());
        assertFalse(r.isAlive());

        // Resurrect with setShields
        r.setShields(5);
        assertEquals(5, r.getShields());
        assertEquals("NORMAL", r.getStatus());
        assertTrue(r.isAlive());
    }

    @Test
    @DisplayName("isAlive returns correct status")
    public void testIsAlive() {
        Robot r = new Robot("R1", "normal", 2, 5);
        assertTrue(r.isAlive());

        r.takeDamage();
        assertTrue(r.isAlive());

        r.takeDamage();
        assertFalse(r.isAlive());
    }
}


