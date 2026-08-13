package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.config.WorldConfig;
import za.co.wethinkcode.robots.server.world.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldConfigTest {

    @Test
    void defaultValuesAreSetCorrectly() {
        WorldConfig config = new WorldConfig();

        // Verify every single default value matches the hardcoded defaults in the class
        assertEquals(5000, config.getPort());
        assertEquals(25, config.getWorldSize());
        assertEquals(1, config.getMazeType());
        assertEquals(3, config.getRepairTime());
        assertEquals(5, config.getReloadTime());
        assertEquals(5, config.getVisibility());
        assertEquals(5, config.getMaxShields());
        assertEquals(5, config.getMaxShots());
        assertEquals(3, config.getObstacleCount());

        // Verify the custom obstacles list is initialized and empty
        List<Position> obstacles = config.getCustomObstacles();
        assertNotNull(obstacles);
        assertTrue(obstacles.isEmpty());
    }
}