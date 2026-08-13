package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("World Tests")
public class WorldTest {

    @Test
    @DisplayName("Constructor sets width, height and initial objects")
    public void testConstructorAndGetters() {
        WorldConfig config = new WorldConfig();
        config.setWidth(10);
        config.setHeight(20);
        WorldConfig.WorldObjectConfig rock = new WorldConfig.WorldObjectConfig();
        rock.setX(2);
        rock.setY(3);
        rock.setType("Rock");
        config.setObstacles(List.of(rock));

        World w = new World(config);
        assertEquals(10, w.getWidth());
        assertEquals(20, w.getHeight());
        assertEquals(1, w.getObjects().size());
        assertEquals("Rock", w.getObjects().get(0).getType());
    }
    @Test
    @DisplayName("isInsideWorld returns correct boolean for coordinates")
    public void testIsInsideWorld() {
        World w = new World(100, 100);
        assertTrue(w.isInsideWorld(0, 0));
        assertTrue(w.isInsideWorld(50, 50));
        assertTrue(w.isInsideWorld(-50, -50));

        assertFalse(w.isInsideWorld(-51, 0));
        assertFalse(w.isInsideWorld(51, 0));
        assertFalse(w.isInsideWorld(0, 51));
    }
}
