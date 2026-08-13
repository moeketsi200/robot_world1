package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.BackCommand;
import za.co.wethinkcode.robots.server.commands.ForwardCommand;
import za.co.wethinkcode.robots.server.commands.RepairCommand;
import za.co.wethinkcode.robots.server.commands.TurnCommand;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class RepairCommandTest {

    static class InstantRepairCommand extends RepairCommand {
        int recordedSleepSeconds = -1;

        InstantRepairCommand(String robotName) {
            super(robotName);
        }

        @Override
        protected void sleepForRepair(int seconds) {
            recordedSleepSeconds = seconds; // capture but do not block
        }
    }

    @Test
    void worldStoresRepairTime() {
        assertEquals(2, new World(21, 0, 2, 5).getRepairTime());
    }

    @Test
    void worldRepairTimeDefaultsToThreeSeconds() {
        assertEquals(3, new World(21, 0).getRepairTime());
    }

    @Test
    void worldRepairTimeCanBeChanged() {
        World world = new World(21, 0, 2, 5);
        world.setRepairTime(10);
        assertEquals(10, world.getRepairTime());
    }

    @Test
    void repairCommandPassesWorldRepairTimeToSleep() {
        World world = new World(21, 0, 7, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(1);

        InstantRepairCommand cmd = new InstantRepairCommand("HAL");
        cmd.execute(world);

        assertEquals(7, cmd.recordedSleepSeconds);
    }

    //Shields restored to full regardless of damage

    @Test
    void repairRestoresFullShieldsAfterOneDamage() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(1);

        new InstantRepairCommand("HAL").execute(world);

        assertEquals(5, robot.getShields());
    }

    @Test
    void repairRestoresFullShieldsAfterHeavyDamage() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(4);

        new InstantRepairCommand("HAL").execute(world);

        assertEquals(5, robot.getShields());
    }

    @Test
    void repairTakesSameTimeRegardlessOfDamage() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);

        robot.takeDamage(1);
        InstantRepairCommand lightCmd = new InstantRepairCommand("HAL");
        lightCmd.execute(world);

        robot.takeDamage(4);
        InstantRepairCommand heavyCmd = new InstantRepairCommand("HAL");
        heavyCmd.execute(world);

        assertEquals(lightCmd.recordedSleepSeconds, heavyCmd.recordedSleepSeconds);
    }

    @Test
    void repairReturnsDone() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(2);

        assertEquals("Done", new InstantRepairCommand("HAL").execute(world));
    }

    @Test
    void repairSucceedsWhenShieldsAlreadyFull() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);

        assertEquals("Done", new InstantRepairCommand("HAL").execute(world));
        assertEquals(5, robot.getShields());
    }

    //Shields cannot exceed maximum

    @Test
    void shieldsDoNotExceedMaximumAfterRepair() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);

        new InstantRepairCommand("HAL").execute(world);

        assertEquals(5, robot.getShields());
    }

    @Test
    void repairUsesRobotMaxNotWorldDefault() {
        World world = new World(21, 0, 2, 5);
        world.clearForTesting();
        Robot tank = new Robot("Tank", "Heavy", 10, 1);
        world.addRobot(tank);
        tank.setShields(tank.getMaxShields());
        tank.takeDamage(7);

        new InstantRepairCommand("Tank").execute(world);

        assertEquals(10, tank.getShields());
    }

    //Robot cannot move while repairing

    @Test
    void robotStatusIsRepairDuringRepair() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(1);

        final Status[] captured = new Status[1];
        RepairCommand cmd = new RepairCommand("HAL") {
            @Override
            protected void sleepForRepair(int seconds) {
                captured[0] = findRobot(world).getStatus();
            }
        };
        cmd.execute(world);

        assertEquals(Status.REPAIR, captured[0]);
    }

    @Test
    void robotStatusIsNormalAfterRepair() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(1);

        new InstantRepairCommand("HAL").execute(world);

        assertEquals(Status.NORMAL, robot.getStatus());
    }

    @Test
    void forwardBlockedWhileRepairing() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.REPAIR);

        String result = new ForwardCommand("HAL", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("repairing"));
    }

    @Test
    void backBlockedWhileRepairing() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.REPAIR);

        String result = new BackCommand("HAL", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("repairing"));
    }

    @Test
    void turnBlockedWhileRepairing() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.setStatus(Status.REPAIR);

        String result = new TurnCommand("HAL", "left").execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("repairing"));
    }

    @Test
    void forwardWorksAfterRepairCompletes() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(2);
        robot.setPosition(new Position(5, 5));

        new InstantRepairCommand("HAL").execute(world);

        assertFalse(new ForwardCommand("HAL", 1).execute(world).startsWith("ERROR"));
    }

    @Test
    void positionUnchangedAfterRepair() {
        World world = new World(21, 0, 2, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(2);
        Position before = robot.getPosition();

        new InstantRepairCommand("HAL").execute(world);

        assertEquals(before, robot.getPosition());
    }

    //Error case

    @Test
    void repairReturnsErrorForUnknownRobot() {
        World world = new World(21, 0, 2, 5);
        String result = new InstantRepairCommand("Ghost").execute(world);
        assertTrue(result.startsWith("ERROR"));
        assertTrue(result.contains("not found"));
    }

    //Real timing

    @Test
    void repairActuallyWaitsForConfiguredDuration() {
        World world = new World(21, 0, 1, 5);
        Robot robot = new Robot("HAL", "Scout", 5, 3);
        world.addRobot(robot);
        robot.takeDamage(2);

        long start = System.currentTimeMillis();
        new RepairCommand("HAL").execute(world);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed >= 900, "Expected ~1s but got " + elapsed + "ms");
        assertEquals(5, robot.getShields());
    }
}
