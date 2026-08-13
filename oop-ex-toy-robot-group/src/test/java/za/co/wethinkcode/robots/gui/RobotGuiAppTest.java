package za.co.wethinkcode.robots.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GUI App Logic Tests")
public class RobotGuiAppTest {

    @Test
    @DisplayName("Should extract position from an array format [x, y]")
    public void testRobotDTOPositionArray() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();
        dto.position = new int[] { 10, -20 };

        assertEquals(10, dto.getX());
        assertEquals(-20, dto.getY());
    }

    @Test
    @DisplayName("Should extract position from separate x and y fields")
    public void testRobotDTOPositionFields() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();
        dto.x = 15;
        dto.y = 25;

        assertEquals(15, dto.getX());
        assertEquals(25, dto.getY());
    }

    @Test
    @DisplayName("Should extract shields and shots directly from root level")
    public void testRobotDTOShieldsRoot() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();
        dto.shields = 3;
        dto.shots = 2;

        assertEquals(3, dto.getShields());
        assertEquals(2, dto.getShots());
    }

    @Test
    @DisplayName("Should extract shields and shots from nested state object")
    public void testRobotDTOShieldsNested() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();
        dto.state = new RobotGuiApp.RobotStateNestedDTO();
        dto.state.shields = 4;
        dto.state.shots = 1;

        assertEquals(4, dto.getShields());
        assertEquals(1, dto.getShots());
    }

    @Test
    @DisplayName("Should successfully deserialize JSON into GameStateResponse")
    public void testGameStateResponseDeserialization() throws Exception {
        String json = "{\"robots\": [{\"name\":\"Hal\", \"position\":[5,5], \"shields\":5, \"shots\":5}]}";
        ObjectMapper mapper = new ObjectMapper();
        RobotGuiApp.GameStateResponse resp = mapper.readValue(json, RobotGuiApp.GameStateResponse.class);

        assertNotNull(resp.robots);
        assertEquals(1, resp.robots.size());
        assertEquals("Hal", resp.robots.get(0).name);
        assertEquals(5, resp.robots.get(0).getX());
    }

    @Test
    @DisplayName("Should return 0 for coordinates if not provided")
    public void testRobotDTODefaultCoordinates() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();

        assertEquals(0, dto.getX());
        assertEquals(0, dto.getY());
    }

    @Test
    @DisplayName("Should return 5 for shields and shots if not provided")
    public void testRobotDTODefaultShieldsAndShots() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();

        assertEquals(5, dto.getShields());
        assertEquals(5, dto.getShots());
    }

    @Test
    @DisplayName("Should successfully deserialize JSON with nested state array")
    public void testGameStateResponseNestedState() throws Exception {
        String json = "{\"state\": {\"robots\": [{\"name\":\"NestedBot\"}]}}";
        ObjectMapper mapper = new ObjectMapper();
        RobotGuiApp.GameStateResponse resp = mapper.readValue(json, RobotGuiApp.GameStateResponse.class);

        assertNotNull(resp.state);
        assertNotNull(resp.state.robots);
        assertEquals(1, resp.state.robots.size());
        assertEquals("NestedBot", resp.state.robots.get(0).name);
    }

    @Test
    @DisplayName("Should successfully deserialize JSON with generic data node")
    public void testGameStateResponseDataNode() throws Exception {
        String json = "{\"data\": [{\"name\":\"DataBot\"}]}";
        ObjectMapper mapper = new ObjectMapper();
        RobotGuiApp.GameStateResponse resp = mapper.readValue(json, RobotGuiApp.GameStateResponse.class);

        assertNotNull(resp.data);
        assertTrue(resp.data.isArray());
        assertEquals("DataBot", resp.data.get(0).get("name").asText());
    }

    @Test
    @DisplayName("Should successfully deserialize JSON into WorldConfig")
    public void testWorldConfigDeserialization() throws Exception {
        String json = "{\"width\": 200, \"height\": 200, \"visibility\": 10, \"obstacles\": [{\"x\": 5, \"y\": -5, \"type\": \"Rock\"}]}";
        ObjectMapper mapper = new ObjectMapper();
        RobotGuiApp.WorldConfig config = mapper.readValue(json, RobotGuiApp.WorldConfig.class);

        assertEquals(200, config.width);
        assertEquals(200, config.height);
        assertEquals(10, config.visibility);
        assertNotNull(config.obstacles);
        assertEquals(1, config.obstacles.size());
        assertEquals(5, config.obstacles.get(0).x);
        assertEquals(-5, config.obstacles.get(0).y);
        assertEquals("Rock", config.obstacles.get(0).type);
    }

    @Test
    @DisplayName("Should handle WorldConfig with missing fields gracefully")
    public void testWorldConfigPartial() throws Exception {
        String json = "{\"width\": 100}";
        ObjectMapper mapper = new ObjectMapper();
        RobotGuiApp.WorldConfig config = mapper.readValue(json, RobotGuiApp.WorldConfig.class);

        assertEquals(100, config.width);
        assertEquals(0, config.height); // default int value
        assertNull(config.obstacles);
    }

    @Test
    @DisplayName("Should successfully match a fire hit message with positive coordinates")
    public void testFireHitRegexPositive() {
        String message = "[SniperBot] Fired! Hit robot2 at [15, 5] (Distance: 3). robot2 took damage!";
        java.util.regex.Matcher m = RobotGuiApp.FIRE_HIT_PATTERN.matcher(message);
        assertTrue(m.find());
        assertEquals("SniperBot", m.group(1));
        assertEquals("15", m.group(2));
        assertEquals("5", m.group(3));
        assertEquals("3", m.group(4));
    }

    @Test
    @DisplayName("Should successfully match a fire hit message with negative coordinates")
    public void testFireHitRegexNegative() {
        String message = "[Rooi] Fired! Hit Edge of world at [-10, -25] (Distance: 1).";
        java.util.regex.Matcher m = RobotGuiApp.FIRE_HIT_PATTERN.matcher(message);
        assertTrue(m.find());
        assertEquals("Rooi", m.group(1));
        assertEquals("-10", m.group(2));
        assertEquals("-25", m.group(3));
        assertEquals("1", m.group(4));
    }

    @Test
    @DisplayName("Should successfully match a fire miss message")
    public void testFireMissRegex() {
        String message = "[Katli] Fired! Missed.";
        java.util.regex.Matcher m = RobotGuiApp.FIRE_MISS_PATTERN.matcher(message);
        assertTrue(m.find());
        assertEquals("Katli", m.group(1));
    }

    @Test
    @DisplayName("Should not match fire hit pattern for a miss message")
    public void testFireHitRegexInvalid() {
        String message = "[Rooi] Fired! Missed.";
        java.util.regex.Matcher m = RobotGuiApp.FIRE_HIT_PATTERN.matcher(message);
        assertFalse(m.find());
    }

    @Test
    @DisplayName("Should not match fire miss pattern for a hit message")
    public void testFireMissRegexInvalid() {
        String message = "[Rooi] Fired! Hit Edge of world at [-10, -25] (Distance: 1).";
        java.util.regex.Matcher m = RobotGuiApp.FIRE_MISS_PATTERN.matcher(message);
        assertFalse(m.find());
    }

    @Test
    @DisplayName("Should fall back to individual y if root position array is too short")
    public void testRobotDTOPositionArrayTooShort() {
        RobotGuiApp.RobotDTO dto = new RobotGuiApp.RobotDTO();
        dto.position = new int[] { 10 }; // Array of length 1 (invalid format for y)
        dto.x = 5;
        dto.y = -5;

        // getX returns position[0] (10) because length > 0
        assertEquals(10, dto.getX());
        // getY falls back to y (-5) because length is not > 1
        assertEquals(-5, dto.getY());
    }
}
