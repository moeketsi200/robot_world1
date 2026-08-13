package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.commands.LaunchCommand;
import za.co.wethinkcode.robots.server.commands.StateCommand;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

// Tests for StateCommand
class StateCommandTest {

    private World world;

    @BeforeEach
    void setUp() {
        world = new World(20, 20);
        world.getObstacles().clear();
        new LaunchCommand("Bot", "Scout", 3, 3).execute(world);
    }

    @Test
    void stateContainsDirection() {
        String result = new StateCommand("Bot").execute(world);
        assertTrue(result.contains("NORTH") || result.contains("SOUTH")
                || result.contains("EAST") || result.contains("WEST"));
    }

    @Test
    void stateContainsShields() {
        String result = new StateCommand("Bot").execute(world);
        String[] parts = result.split("\\|");
        assertNotNull(parts[2]); // shields is the third field
    }

    @Test
    void stateContainsShots() {
        String result = new StateCommand("Bot").execute(world);
        String[] parts = result.split("\\|");
        assertNotNull(parts[3]); // shots is the fourth fieldl
    }

    @Test
    void stateContainsStatus() {
        String result = new StateCommand("Bot").execute(world);
        assertTrue(result.contains("NORMAL"));
    }

    @Test
    void stateResponseUsesProtocolTypes() {
        StateCommand command = new StateCommand("Bot");
        JsonObject response = command.buildResponse(world, command.execute(world));

        JsonArray position = response.getAsJsonArray("position");
        assertEquals(2, position.size());
        assertTrue(position.get(0).isJsonPrimitive());
        assertTrue(position.get(0).getAsJsonPrimitive().isNumber());
        assertTrue(response.get("shields").getAsJsonPrimitive().isNumber());
        assertTrue(response.get("shots").getAsJsonPrimitive().isNumber());
    }

    @Test
    void stateReturnsErrorForUnknownRobot() {
        String result = new StateCommand("Nobody").execute(world);
        assertTrue(result.startsWith("ERROR"));
    }
}
