package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WorldObject Tests")
public class WorldObjectTest {

    @Test
    @DisplayName("Getters return values set in constructor")
    public void testGetters() {
        WorldObject o = new WorldObject(5, 6, "Tree");
        assertEquals(5, o.getX());
        assertEquals(6, o.getY());
        assertEquals("Tree", o.getType());
    }
}
