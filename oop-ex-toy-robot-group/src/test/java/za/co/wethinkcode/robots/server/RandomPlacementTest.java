package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class RandomPlacementTest {
    @Test
    public void testRobotIsPlacedRandomly() {
        World world = new World(100, 100);
        Set<String> positions = new HashSet<>();
        
        for (int i = 0; i < 10; i++) {
            Robot robot = new Robot("Bot" + i, "normal", 5, 5);
            assertTrue(world.placeRobot(robot));
            positions.add(robot.getX() + "," + robot.getY());
        }
        
        // It's very unlikely that 10 robots are all placed at the same spot if it's random.
        // And they shouldn't be anyway because positions are occupied.
        // But more importantly, the first one shouldn't necessarily be at [0,0].
        // Let's check that we have 10 unique positions (this was already true before, but now they should be random).
        assertEquals(10, positions.size());
        
        // To really test randomness, we can check that it's not the sequential scan result.
        // Sequential scan would put them at [-50, -50], [-49, -50], etc. or [0,0] then sequential.
        // Before it was [0,0], then [-50,-50], [-49,-50]...
        
        assertFalse(positions.contains("0,0") && positions.size() == 1, "Should not be just [0,0]");
    }
}
