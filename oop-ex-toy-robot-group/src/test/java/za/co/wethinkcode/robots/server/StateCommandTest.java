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

@DisplayName("State Command Tests")
public class StateCommandTest {
    private Server server;
    private StateCommand stateCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        stateCommand = new StateCommand(server);
    }

    @Test
    @DisplayName("Should return error if no robot is launched")
    public void testStateWithoutRobot() {
        CommandRequest request = new CommandRequest("state", List.of());
        String response = stateCommand.execute(request, mockSocket);
        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should return the formatted state of the launched robot")
    public void testStateWithRobot() {
        Robot robot = new Robot("StateBot", "normal", 5, 5);
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        CommandRequest request = new CommandRequest("state", List.of());
        String response = stateCommand.execute(request, mockSocket);

        assertTrue(response.contains("[StateBot] (normal)"));
        assertTrue(response.contains("Facing: NORTH"));
    }
}