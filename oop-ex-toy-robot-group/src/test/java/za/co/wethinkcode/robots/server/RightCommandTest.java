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

@DisplayName("Right Command Tests")
public class RightCommandTest {
    private Server server;
    private RightCommand rightCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        rightCommand = new RightCommand(server);
    }

    @Test
    @DisplayName("Should return an error if turning without a launched robot")
    public void testTurnWithoutRobot() {
        CommandRequest request = new CommandRequest("right", List.of());
        String response = rightCommand.execute(request, mockSocket);
        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should turn right correctly (EAST -> SOUTH)")
    public void testTurnRight() {
        Robot robot = new Robot("Righty", "normal", 5, 5);
        robot.setDirection("EAST");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        CommandRequest request = new CommandRequest("right", List.of());
        String response = rightCommand.execute(request, mockSocket);

        assertTrue(response.contains("Now facing SOUTH"));
        assertEquals("SOUTH", robot.getDirection());
    }
}