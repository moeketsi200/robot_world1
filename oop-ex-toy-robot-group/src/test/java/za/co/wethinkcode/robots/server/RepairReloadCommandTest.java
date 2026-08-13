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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repair and Reload Command Tests")
public class RepairReloadCommandTest {

    private Server server;
    private RepairCommand repairCommand;
    private ReloadCommand reloadCommand;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new Server(5000);
        repairCommand = new RepairCommand(server);
        reloadCommand = new ReloadCommand(server);
    }

    @Test
    @DisplayName("Repair restores shields to max and sets status normal")
    public void testRepairRestoresShields() {
        Robot r = new Robot("R1", "normal", 5, 5);
        r.setPosition(0,0);
        server.getWorld().addRobot(r);
        server.addClientRobot(mockSocket, r);

        // Damage the robot
        r.takeDamage(3);
        assertEquals(2, r.getShields());
        assertEquals("NORMAL", r.getStatus());

        CommandRequest req = new CommandRequest("repair", List.of());
        String resp = repairCommand.execute(req, mockSocket);

        assertTrue(resp.contains("Repair complete"));
        assertEquals(r.getMaxShields(), r.getShields());
        assertEquals("NORMAL", r.getStatus());
    }

    @Test
    @DisplayName("Reload restores shots to max and sets status normal")
    public void testReloadRestoresShots() {
        Robot r = new Robot("R2", "normal", 5, 5);
        r.setPosition(1,1);
        server.getWorld().addRobot(r);
        server.addClientRobot(mockSocket, r);

        // Deplete ammo
        for (int i = 0; i < 5; i++) r.fireWeapon();
        assertEquals(0, r.getShots());

        CommandRequest req = new CommandRequest("reload", List.of());
        String resp = reloadCommand.execute(req, mockSocket);

        assertTrue(resp.contains("Reload complete"));
        assertEquals(r.getMaxShots(), r.getShots());
        assertEquals("NORMAL", r.getStatus());
    }

    @Test
    @DisplayName("Cannot repair or reload a dead robot")
    public void testCannotRepairOrReloadDeadRobot() {
        Robot r = new Robot("R3", "normal", 1, 5);
        r.setPosition(2,2);
        server.getWorld().addRobot(r);
        server.addClientRobot(mockSocket, r);

        // Kill the robot
        r.takeDamage(1);
        assertEquals(0, r.getShields());
        assertEquals("DEAD", r.getStatus());

        CommandRequest reqRepair = new CommandRequest("repair", List.of());
        String respRepair = repairCommand.execute(reqRepair, mockSocket);
        assertTrue(respRepair.contains("Cannot repair a dead robot"));

        CommandRequest reqReload = new CommandRequest("reload", List.of());
        String respReload = reloadCommand.execute(reqReload, mockSocket);
        assertTrue(respReload.contains("Cannot reload a dead robot"));
    }

    @Test
    @DisplayName("Killed robot is removed from the board")
    public void testKilledRobotRemovedFromBoard() {
        Robot shooter = new Robot("Shooter", "normal", 5, 5);
        shooter.setPosition(10, 10);
        server.getWorld().addRobot(shooter);
        server.addClientRobot(mockSocket, shooter);

        Robot target = new Robot("Target", "normal", 1, 5);
        target.setPosition(10, 12);
        server.getWorld().addRobot(target);

        CommandRequest req = new CommandRequest("fire", List.of());
        FireCommand fire = new FireCommand(server);
        String resp = fire.execute(req, mockSocket);

        // target should be dead and removed from world
        assertEquals(0, target.getShields());
        assertEquals("DEAD", target.getStatus());
        assertFalse(server.getWorld().getRobots().contains(target));
        assertTrue(resp.contains("has been destroyed and removed from the board"));
    }
}
