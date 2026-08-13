package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovementCommandTest {
    private Server server;
    private Socket mockSocket;
    private Robot robot;

    @BeforeEach
    void setUp() {
        server = new Server(5000);
        mockSocket = mock(Socket.class);
        robot = new Robot("TestBot", "normal", 5, 5);
        robot.setPosition(10, 10);
        server.addClientRobot(mockSocket, robot);
        server.getWorld().addRobot(robot);
    }

    @Test
    void testForwardCommandWithRobotName() {
        ForwardCommand forward = new ForwardCommand(server);
        CommandRequest request = new CommandRequest("forward", List.of("TestBot", "10"));
        String response = forward.execute(request, mockSocket);
        
        assertTrue(response.contains("Moved forward 10 steps"));
        assertEquals(10, robot.getX());
        assertEquals(20, robot.getY());
    }

    @Test
    void testBackCommand() {
        BackCommand back = new BackCommand(server);
        CommandRequest request = new CommandRequest("back", List.of("3"));
        String response = back.execute(request, mockSocket);
        
        assertTrue(response.contains("Moved back 3 steps"));
        assertEquals(10, robot.getX());
        assertEquals(7, robot.getY());
    }

    @Test
    void testLeftCommand() {
        LeftCommand left = new LeftCommand(server);
        CommandRequest request = new CommandRequest("left", List.of());
        String response = left.execute(request, mockSocket);
        
        assertTrue(response.toLowerCase().contains("turned left"));
        assertEquals("WEST", robot.getDirection());
    }

    @Test
    void testRightCommand() {
        RightCommand right = new RightCommand(server);
        CommandRequest request = new CommandRequest("right", List.of());
        String response = right.execute(request, mockSocket);
        
        assertTrue(response.toLowerCase().contains("turned right"));
        assertEquals("EAST", robot.getDirection());
    }

    @Test
    void testTurnCommandGeneric() {
        TurnCommand turn = new TurnCommand(server);
        
        CommandRequest leftRequest = new CommandRequest("turn", List.of("left"));
        String leftResponse = turn.execute(leftRequest, mockSocket);
        assertTrue(leftResponse.toLowerCase().contains("turned left"));
        assertEquals("WEST", robot.getDirection());

        CommandRequest rightRequest = new CommandRequest("turn", List.of("right"));
        String rightResponse = turn.execute(rightRequest, mockSocket);
        assertTrue(rightResponse.toLowerCase().contains("turned right"));
        assertEquals("NORTH", robot.getDirection());
    }

    @Test
    void testTurnCommandWithRobotName() {
        TurnCommand turn = new TurnCommand(server);
        CommandRequest request = new CommandRequest("turn", List.of("TestBot", "left"));
        String response = turn.execute(request, mockSocket);
        assertTrue(response.toLowerCase().contains("turned left"));
        assertEquals("WEST", robot.getDirection());
    }
}
