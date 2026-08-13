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
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Fire Command Tests")
public class FireCommandTest {

    private Server server;
    private FireCommand fireCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Initialize a standard server instance (creates a default 200x200 world)
        server = new Server(5000);
        fireCommand = new FireCommand(server);
    }

    @Test
    @DisplayName("Should return an error if firing without a launched robot")
    public void testFireWithoutRobot() {
        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);
        
        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should report a miss if bullet travels 5 steps hitting nothing")
    public void testFireAndMiss() {
        // Setup: Place a robot in an open area
        Robot shooter = new Robot("Shooter", "normal", 5, 5);
        shooter.setPosition(10, 10);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        assertTrue(response.contains("[Shooter] Fired! Missed."));
    }

    @Test
    @DisplayName("Should report out of shots if ammo reaches 0")
    public void testFireWithNoAmmo() {
        // Setup: Place a robot and exhaust its ammo
        Robot shooter = new Robot("EmptyGun", "normal", 5, 5);
        shooter.setPosition(10, 10);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        // Deplete all 5 shots manually
        for (int i = 0; i < 5; i++) {
            shooter.fireWeapon();
        }

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        assertTrue(response.contains("[EmptyGun] Out of shots!"));
    }

    @Test
    @DisplayName("Should hit the edge of the world")
    public void testHitEdgeOfWorld() {
        Robot shooter = new Robot("EdgeBot", "normal", 5, 5);
        // Robot faces NORTH by default. Place it 2 steps away from the top boundary (Y = 100 in a 200x200 world)
        shooter.setPosition(10, 98);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        assertTrue(response.contains("Hit Edge of world at [10, 101]"));
    }

    @Test
    @DisplayName("Should hit an obstacle in its path")
    public void testHitObstacle() {
        Robot shooter = new Robot("Sniper", "normal", 5, 5);
        shooter.setPosition(5, 5);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        // Manually place an obstacle 3 steps NORTH of the robot
        server.getWorld().getObjects().add(new WorldObject(5, 8, "Rock"));

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        assertTrue(response.contains("Hit Rock at [5, 8] (Distance: 3)"));
    }

    @Test
    @DisplayName("Should hit another robot in its path")
    public void testHitOtherRobot() {
        Robot shooter = new Robot("Attacker", "normal", 5, 5);
        shooter.setPosition(20, 20);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        // Place a target robot 4 steps NORTH
        Robot target = new Robot("TargetBot", "normal", 5, 5);
        target.setPosition(20, 24);
        server.getWorld().addRobot(target);

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        assertTrue(response.contains("Hit Robot TargetBot at [20, 24] (Distance: 4)"));
    }

    @Test
    @DisplayName("Should apply damage to target robot when hit")
    public void testDamageAppliedToTargetRobot() {
        Robot shooter = new Robot("Attacker", "normal", 5, 5);
        shooter.setPosition(20, 20);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        // Place a target robot with 3 shields
        Robot target = new Robot("TargetBot", "normal", 3, 5);
        target.setPosition(20, 24);
        assertEquals(3, target.getShields());
        assertEquals("NORMAL", target.getStatus());
        server.getWorld().addRobot(target);

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        // Verify damage was applied
        assertEquals(2, target.getShields());
        assertEquals("NORMAL", target.getStatus());
        assertTrue(response.contains("TargetBot took damage"));
        assertTrue(response.contains("Shields: 2"));
        assertTrue(response.contains("Status: NORMAL"));
    }

    @Test
    @DisplayName("Should kill target robot when shields reach 0")
    public void testTargetRobotKilledWhenShieldsExhausted() {
        Robot shooter = new Robot("Attacker", "normal", 5, 10);
        shooter.setPosition(20, 20);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        // Place a target robot with 1 shield
        Robot target = new Robot("TargetBot", "normal", 1, 5);
        target.setPosition(20, 24);
        assertEquals(1, target.getShields());
        assertEquals("NORMAL", target.getStatus());
        server.getWorld().addRobot(target);

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        // Verify robot died
        assertEquals(0, target.getShields());
        assertEquals("DEAD", target.getStatus());
        assertTrue(response.contains("TargetBot took damage"));
        assertTrue(response.contains("Shields: 0"));
        assertTrue(response.contains("Status: DEAD"));
    }

    @Test
    @DisplayName("Should not apply damage to obstacles")
    public void testNoDamageToObstacles() {
        Robot shooter = new Robot("Sniper", "normal", 5, 5);
        shooter.setPosition(5, 5);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        // Manually place an obstacle 3 steps NORTH of the robot
        WorldObject obstacle = new WorldObject(5, 8, "Rock");
        server.getWorld().getObjects().add(obstacle);

        CommandRequest request = new CommandRequest("fire", List.of());
        String response = fireCommand.execute(request, mockSocket);

        // Verify no damage to obstacle (just report the hit)
        assertTrue(response.contains("Hit Rock at [5, 8] (Distance: 3)"));
        assertTrue(!response.contains("took damage"));
    }
}