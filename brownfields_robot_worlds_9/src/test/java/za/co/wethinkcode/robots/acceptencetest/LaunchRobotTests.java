package za.co.wethinkcode.robots.acceptencetest;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import za.co.wethinkcode.robots.RobotWorldClient;
import za.co.wethinkcode.robots.RobotWorldJsonClient;

/**
 * As a player
 * I want to launch my robot in the online robot world
 * So that I can break the record for the most robot kills
 */
public class LaunchRobotTests {
    private final static int DEFAULT_PORT = 5000;
    private final static String DEFAULT_IP = "localhost";
    private final RobotWorldClient serverClient = new RobotWorldJsonClient();

    @BeforeEach
    void connectToServer(){
        serverClient.connect(DEFAULT_IP, DEFAULT_PORT);
    }

    @AfterEach
    void disconnectFromServer(){
        serverClient.disconnect();
    }

    @Test
    void validLaunchShouldSucceed(){
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // When I send a valid launch request to the server
        JsonNode response = serverClient.sendRequest(makeLaunchJson(new LaunchSpec("HAL")));

        // Then I should get a valid response from the server with position and state
        assertLaunchSuccess(response);
    }


    @Test
    void canLaunchAnotherRobot() {
        // Given a world of size 2x2
        // and robot "HAL" has already been launched into the world
        assertTrue(serverClient.isConnected());
        JsonNode halResponse = serverClient.sendRequest(makeLaunchJson(new LaunchSpec("HAL")));
        assertLaunchSuccess(halResponse);

        // When I launch robot "R2D2" into the world (as a second player, second connection)
        RobotWorldClient secondClient = new RobotWorldJsonClient();
        secondClient.connect(DEFAULT_IP, DEFAULT_PORT);
        try {
            assertTrue(secondClient.isConnected());
            JsonNode r2d2Response = secondClient.sendRequest(makeLaunchJson(new LaunchSpec("R2D2")));

            // Then the launch should be successful
            // and a randomly allocated position of R2D2 should be returned
            assertLaunchSuccess(r2d2Response);
        } finally {
            secondClient.disconnect();
        }
    }

    @Test
    void invalidLaunchShouldFail(){
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // When I send an invalid launch request with the command "luanch" instead of "launch"
        LaunchSpec invalidSpec = new LaunchSpec("HAL", "luanch", "shooter", "5", "5");
        JsonNode response = serverClient.sendRequest(makeLaunchJson(invalidSpec));

        // Then I should get an error response with "Unsupported command"
        assertLaunchError(response, "Unsupported command");
    }

    @Test
    void noMoreSpaceInTheWorldForAnotherRobot() {
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // And I have already launched a robot
        JsonNode firstResponse = serverClient.sendRequest(makeLaunchJson(new LaunchSpec("HAL")));
        assertEquals("OK", firstResponse.get("result").asText());

        // When I try to launch a second robot on the same connection
        JsonNode response = serverClient.sendRequest(makeLaunchJson(new LaunchSpec("OVERFLOW")));

        // Then I should get an error response
        assertLaunchError(response);
    }

    @Test
    void robotWithSameNameAlreadyInTheWorld() {
        // Given that I am connected to a running Robot Worlds server
        assertTrue(serverClient.isConnected());

        // And a robot named "HAL" already exists in the world
        JsonNode firstResponse = serverClient.sendRequest(makeLaunchJson(new LaunchSpec("HAL")));
        assertEquals("OK", firstResponse.get("result").asText());

        // When I try to launch another robot with the same name
        JsonNode response = serverClient.sendRequest(makeLaunchJson(new LaunchSpec("HAL")));

        // Then I should get an error response
        assertLaunchError(response);
    }

    @Test
    void launchRobotsIntoWorldWithObstacle() {
        java.util.List<RobotWorldClient> clients = new java.util.ArrayList<>();
        try {
            for (int i = 1; i <= 8; i++) {
                RobotWorldClient client = new RobotWorldJsonClient();
                client.connect(DEFAULT_IP, DEFAULT_PORT);
                assertTrue(client.isConnected(), "Client " + i + " should be connected.");
                clients.add(client);

                String robotName = "HAL" + i;
                JsonNode response = client.sendRequest(makeLaunchJson(new LaunchSpec(robotName)));

                assertLaunchSuccess(response);
                assertRobotNotAtPosition(response, 1, 1, robotName);
            }
        } finally {
            for (RobotWorldClient client : clients) {
                client.disconnect();
            }
        }
    }

    @Test
    void worldWithoutObstaclesIsFull(){
        //Given a world of size 2x2
        // and i have successfully launched 9 robots into the world
        List<RobotWorldClient> clients = new java.util.ArrayList<>();
        try {
            for (int i = 1; i <= 9; i++) {
                RobotWorldClient client = new RobotWorldJsonClient();
                client.connect(DEFAULT_IP, DEFAULT_PORT);
                assertTrue(client.isConnected(), "Client " + i + " should be connected.");
                clients.add(client);
                String robotName = "HAL" + i;
                JsonNode response = client.sendRequest(makeLaunchJson(new LaunchSpec(robotName)));
                assertLaunchSuccess(response);
            }

            //When i launch one more robot
            RobotWorldClient overflowClient = new RobotWorldJsonClient();
            overflowClient.connect(DEFAULT_IP, DEFAULT_PORT);
            assertTrue(overflowClient.isConnected());
            clients.add(overflowClient);

            JsonNode overflowResponse = overflowClient.sendRequest(makeLaunchJson(new LaunchSpec("OVERFLOW")));

            //Then i should get an error response back.
            assertLaunchError(overflowResponse, "No more space in this world");
        } finally {
            for (RobotWorldClient client : clients) {
                client.disconnect();
            }
        }
    }

    @Test
    void worldWithAnObstacleIsFull() {
        // Given a world of size 2x2 with an obstacle at [1,1]
        // and I have successfully launched 8 robots into the world
        List<RobotWorldClient> clients = new java.util.ArrayList<>();
        try {
            for (int i = 1; i <= 8; i++) {
                RobotWorldClient client = new RobotWorldJsonClient();
                client.connect(DEFAULT_IP, DEFAULT_PORT);
                assertTrue(client.isConnected(), "Client " + i + " should be connected.");
                clients.add(client);

                String robotName = "HAL" + i;
                JsonNode response = client.sendRequest(makeLaunchJson(new LaunchSpec(robotName)));

                assertLaunchSuccess(response);
                assertRobotNotAtPosition(response, 1, 1, robotName);
            }

            // When I launch one more robot
            RobotWorldClient overflowClient = new RobotWorldJsonClient();
            overflowClient.connect(DEFAULT_IP, DEFAULT_PORT);
            assertTrue(overflowClient.isConnected());
            clients.add(overflowClient);

            JsonNode overflowResponse = overflowClient.sendRequest(makeLaunchJson(new LaunchSpec("OVERFLOW")));

            // Then I should get an error response back with the message "No more space in this world"
            assertLaunchError(overflowResponse, "No more space in this world");
        } finally {
            for (RobotWorldClient client : clients) {
                client.disconnect();
            }
        }
    }

    // --- Parameter Object for Launch Specifications ---

    private record LaunchSpec(String robotName, String command, String kind, String shields, String shots) {
        LaunchSpec(String robotName) {
            this(robotName, "launch", "shooter", "5", "5");
        }

        String toJson() {
            return "{" +
                    "  \"robot\": \"" + robotName + "\"," +
                    "  \"command\": \"" + command + "\"," +
                    "  \"arguments\": [\"" + kind + "\",\"" + shields + "\",\"" + shots + "\"]" +
                    "}";
        }
    }

    // --- Helper Request Creators ---

    private String makeLaunchJson(LaunchSpec spec) {
        return spec.toJson();
    }

    // --- Custom Helper Assertions ---

    private void assertLaunchSuccess(JsonNode response) {
        assertNotNull(response.get("result"));
        assertEquals("OK", response.get("result").asText());
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("position"));
        assertEquals(2, response.get("data").get("position").size());
        assertTrue(response.get("data").get("position").get(0).isNumber());
        assertTrue(response.get("data").get("position").get(1).isNumber());
        assertNotNull(response.get("state"));
    }

    private void assertLaunchError(JsonNode response) {
        assertNotNull(response.get("result"));
        assertEquals("ERROR", response.get("result").asText());
        assertNotNull(response.get("data"));
        assertNotNull(response.get("data").get("message"));
    }

    private void assertLaunchError(JsonNode response, String expectedMessageSubstring) {
        assertLaunchError(response);
        assertTrue(response.get("data").get("message").asText().contains(expectedMessageSubstring));
    }

    private void assertRobotNotAtPosition(JsonNode response, int obstacleX, int obstacleY, String robotName) {
        int x = response.get("data").get("position").get(0).asInt();
        int y = response.get("data").get("position").get(1).asInt();
        assertFalse(x == obstacleX && y == obstacleY,
                "Robot " + robotName + " landed on the obstacle at [" + obstacleX + ", " + obstacleY + "]!");
    }
}