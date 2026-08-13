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

@DisplayName("Push Command Tests")
public class PushCommandTest {
    private Server server;
    private PushCommand pushCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        pushCommand = new PushCommand(server);
    }

    @Test
    @DisplayName("Should return an error if pushing without a launched robot")
    public void testPushWithoutRobot() {
        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);
        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should return an error if non-heavy robot tries to push")
    public void testPushWithNonHeavyRobot() {
        Robot robot = new Robot("Weakling", "normal", 5, 5);
        robot.setDirection("NORTH");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);

        assertTrue(response.contains("Error: Only heavy robots can push!"));
    }

    @Test
    @DisplayName("Should report pushing empty air if nothing is in front")
    public void testPushEmptyAir() {
        Robot robot = new Robot("Bulldozer", "heavy", 5, 5);
        robot.setDirection("NORTH");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);

        assertTrue(response.contains("Pushed empty air!"));
    }

    @Test
    @DisplayName("Should push an obstacle forward successfully")
    public void testPushObjectSuccessfully() {
        Robot robot = new Robot("Bulldozer", "heavy", 0, 0);
        robot.setDirection("NORTH");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        WorldObject rock = new WorldObject(0, 1, "Rock");
        server.getWorld().getObjects().add(rock);

        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);

        assertTrue(response.contains("Pushed Rock forward."));
        assertEquals(0, rock.getX());
        assertEquals(2, rock.getY());
    }

    @Test
    @DisplayName("Should not push an obstacle into another obstacle")
    public void testPushObjectIntoAnother() {
        Robot robot = new Robot("Bulldozer", "heavy", 0, 0);
        robot.setDirection("NORTH");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        WorldObject rock1 = new WorldObject(0, 1, "Rock");
        WorldObject rock2 = new WorldObject(0, 2, "Mountain");
        server.getWorld().getObjects().add(rock1);
        server.getWorld().getObjects().add(rock2);

        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);

        assertTrue(response.contains("Cannot push Rock into another obstacle!"));
        assertEquals(1, rock1.getY()); // Rock1 shouldn't have moved
    }

    @Test
    @DisplayName("Should crush a robot behind the pushed obstacle")
    public void testPushObjectCrushesRobot() {
        Robot heavyRobot = new Robot("Bulldozer", "heavy", 0, 0);
        heavyRobot.setDirection("NORTH");
        server.getWorld().addRobot(heavyRobot);
        server.addClientRobot(mockSocket, heavyRobot);

        WorldObject rock = new WorldObject(0, 1, "Rock");
        server.getWorld().getObjects().add(rock);

        Robot targetRobot = new Robot("Target", "normal", 5, 5);
        targetRobot.setPosition(0, 2);
        server.getWorld().addRobot(targetRobot);

        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);

        assertTrue(response.contains("Pushed Rock forward."));
        assertTrue(response.contains("Target was crushed!"));
        assertEquals(2, rock.getY()); // Rock pushed onto Target's position
    }

    @Test
    @DisplayName("Should not push an obstacle off the edge of the world")
    public void testPushObjectOffEdge() {
        // Find the edge of the world from the server's world
        int topEdge = server.getWorld().getHeight() / 2;
        
        Robot robot = new Robot("Bulldozer", "heavy", 5, 5);
        robot.setPosition(0, topEdge - 1);
        robot.setDirection("NORTH");
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        WorldObject rock = new WorldObject(0, topEdge, "Rock");
        server.getWorld().getObjects().add(rock);

        CommandRequest request = new CommandRequest("push", List.of());
        String response = pushCommand.execute(request, mockSocket);

        assertTrue(response.contains("off the edge of the world!"));
        assertEquals(topEdge, rock.getY()); // Rock shouldn't have moved
    }
}
