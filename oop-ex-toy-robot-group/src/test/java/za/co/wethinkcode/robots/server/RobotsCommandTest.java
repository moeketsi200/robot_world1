package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Robots Command Tests")
public class RobotsCommandTest {
    private Server server;
    private RobotsCommand robotsCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        robotsCommand = new RobotsCommand(server);
    }

    @Test
    @DisplayName("Should return JSON array of robots for client")
    public void testExecuteForClient() {
        Robot robot = new Robot("JsonBot", "sniper", 3, 2);
        robot.setPosition(5, 10);
        server.getWorld().addRobot(robot);

        CommandRequest request = new CommandRequest("robots", List.of());
        // Execute the client JSON handler
        String response = robotsCommand.executeForClient(request, mockSocket);

        assertTrue(response.contains("\"name\":\"JsonBot\""));
        assertTrue(response.contains("\"kind\":\"sniper\""));
        assertTrue(response.contains("\"direction\":\"NORTH\""));
        assertTrue(response.contains("\"shields\":3"));
        assertTrue(response.contains("\"shots\":2"));
        assertTrue(response.contains("\"position\":[5,10]"));
    }
}