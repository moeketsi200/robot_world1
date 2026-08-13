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

@DisplayName("Reload Command Tests")
public class ReloadCommandTest {
    private Server server;
    private ReloadCommand reloadCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        reloadCommand = new ReloadCommand(server);
    }

    @Test
    @DisplayName("Should return an error if reloading without a launched robot")
    public void testReloadWithoutRobot() {
        CommandRequest request = new CommandRequest("reload", List.of());
        String response = reloadCommand.execute(request, mockSocket);

        assertTrue(response.contains("Error: You must launch a robot first."));
    }

    @Test
    @DisplayName("Should block for timer and restore shots")
    public void testReloadRestoresShotsAfterTimer() {
        Robot robot = new Robot("AmmoBot", "normal", 5, 5);
        robot.fireWeapon();
        robot.fireWeapon();
        server.getWorld().addRobot(robot);
        server.addClientRobot(mockSocket, robot);

        long startedAt = System.currentTimeMillis();
        String response = reloadCommand.execute(new CommandRequest("reload", List.of()), mockSocket);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertTrue(elapsed >= 900, "Reload should block for about the configured reload timer");
        assertEquals(5, robot.getShots());
        assertEquals("NORMAL", robot.getStatus());
        assertTrue(response.contains("[AmmoBot] Reload complete. Shots restored to 5."));
    }
}
