package za.co.wethinkcode.robots.acceptencetest;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.RobotWorldClient;
import za.co.wethinkcode.robots.RobotWorldJsonClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story: Look
 * Scenario: The world is empty
 *
 * As a player
 * I want to look around in the world
 * So that I can see obstacles and other robots
 */
public class LookRobotTests {
    private final static int DEFAULT_PORT = 5000;
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    @BeforeEach
    void connectToServer() {
        serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
    }

    @AfterEach
    void disconnectFromServer() {
        serverClient.disconnect();
    }

    @Test
    void lookInEmptyWorldShouldSucceed() {
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // And I have launched a robot into the world
        String launchRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"launch\"," +
                "  \"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        JsonNode launchResponse = serverClient.sendRequest(launchRequest);
        assertEquals("OK", launchResponse.get("result").asText());

        // When I send a valid "look" request to the server
        String lookRequest = "{" +
                "  \"robot\": \"HAL\"," +
                "  \"command\": \"look\"," +
                "  \"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(lookRequest);

        // Then I should get a valid OK response from the server
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());

        // And the response data should contain objects (or sightings) array
        assertNotNull(response.get("data"));
        JsonNode objectsNode = response.get("data").get("objects") != null 
                ? response.get("data").get("objects") 
                : response.get("data").get("sightings");
        assertNotNull(objectsNode);
        assertTrue(objectsNode.isArray());

        // And I should also get the state of the robot
        assertNotNull(response.get("state"));
    }

    @Test
    void lookWithoutLaunchingShouldFail() {
        // Given that I am connected to a running Robot Worlds server without launching a robot
        assertTrue(serverClient.isConnected());

        // When I send a look request for a non-launched robot
        String lookRequest = "{" +
                "  \"robot\": \"Ghost\"," +
                "  \"command\": \"look\"," +
                "  \"arguments\": []" +
                "}";
        JsonNode response = serverClient.sendRequest(lookRequest);

        // Then I should get an error response
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());

        // And an appropriate error message
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("message"));
        String msg = response.get("data").get("message").asText().toLowerCase();
        assertTrue(msg.contains("robot") || msg.contains("launch") || msg.contains("find"));
    }
}
