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

@DisplayName("Repair Command Tests")
public class RepairCommandTest {
    private Server server;
    private RepairCommand repairCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        repairCommand = new RepairCommand(server);
    }

    @Test
    @DisplayName("Should return an error if repairing without a launched robot")
    public void testRepairWithoutRobot() {
        CommandRequest request = new CommandRequest("repair", List.of());
        String response = repairCommand.execute(request, mockSocket);

        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should block for timer and restore shields")
    public void testRepairRestoresShieldsAfterTimer() {
        Robot robot = new Robot("FixBot", "normal", 5, 5);
        robot.takeDamage();
        robot.takeDamage();
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        long startedAt = System.currentTimeMillis();
        String response = repairCommand.execute(new CommandRequest("repair", List.of()), mockSocket);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertTrue(elapsed >= 900, "Repair should block for about the repair timer");
        assertEquals(5, robot.getShields());
        assertEquals("NORMAL", robot.getStatus());
        assertTrue(response.contains("[FixBot] Repair complete. Shields restored to 5."));
    }
}
