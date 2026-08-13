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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Left Command Tests")
public class LeftCommandTest {
    private Server server;
    private LeftCommand leftCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        leftCommand = new LeftCommand(server);
    }

    @Test
    @DisplayName("Should return an error if turning without a launched robot")
    public void testTurnWithoutRobot() {
        CommandRequest request = new CommandRequest("left", List.of());
        String response = leftCommand.execute(request, mockSocket);
        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should turn left correctly (NORTH -> WEST)")
    public void testTurnLeft() {
        Robot robot = new Robot("Lefty", "normal", 5, 5);
        robot.setDirection("NORTH");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        CommandRequest request = new CommandRequest("left", List.of());
        String response = leftCommand.execute(request, mockSocket);

        assertTrue(response.contains("Now facing WEST"));
        assertEquals("WEST", robot.getDirection());
    }
}