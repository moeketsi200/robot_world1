package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.commands.BackCommand;
import za.co.wethinkcode.robots.server.commands.ForwardCommand;
import za.co.wethinkcode.robots.server.commands.ReloadCommand;
import za.co.wethinkcode.robots.server.commands.TurnCommand;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.*;

class ReloadCommandTest {

    // Subclass that skips the real sleep so tests run instantly
    static class InstantReloadCommand extends ReloadCommand {
        int secondsReceived;

        InstantReloadCommand(String name) { super(name); }

        @Override
        protected void sleepForReload(int seconds) {
            secondsReceived = seconds;
        }
    }

    //World configures reload time

    @Test
    void worldStoresReloadTime() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        assertEquals(5, world.getReloadTime());
    }

    @Test
    void reloadTimeIsConfigurable() {
        World world = new World(20, 5, 3, 7);
        world.clearForTesting();
        assertEquals(7, world.getReloadTime());
    }

    @Test
    void commandPassesWorldReloadTimeToSleep() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R1", "Scout", 5, 5);
        world.addRobot(robot);
        robot.reduceShots(1);

        InstantReloadCommand cmd = new InstantReloadCommand("R1");
        cmd.execute(world);

        assertEquals(5, cmd.secondsReceived);
    }

    //Robot cannot move while reloading

    @Test
    void statusIsReloadDuringReload() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R2", "Scout", 5, 5);
        world.addRobot(robot);
        robot.reduceShots(1);

        ReloadCommand cmd = new ReloadCommand("R2") {
            @Override
            protected void sleepForReload(int seconds) {
                assertEquals(Status.RELOAD, robot.getStatus());
            }
        };
        cmd.execute(world);
    }

    @Test
    void statusIsNormalAfterReload() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R3", "Scout", 5, 5);
        world.addRobot(robot);
        robot.reduceShots(1);

        new InstantReloadCommand("R3").execute(world);
        assertEquals(Status.NORMAL, robot.getStatus());
    }

    @Test
    void forwardBlockedDuringReload() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R4", "Scout", 5, 5);
        world.addRobot(robot);
        robot.setStatus(Status.RELOAD);

        String result = new ForwardCommand("R4", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void backBlockedDuringReload() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R5", "Scout", 5, 5);
        world.addRobot(robot);
        robot.setStatus(Status.RELOAD);

        String result = new BackCommand("R5", 1).execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void turnBlockedDuringReload() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R6", "Scout", 5, 5);
        world.addRobot(robot);
        robot.setStatus(Status.RELOAD);

        String result = new TurnCommand("R6", "left").execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    @Test
    void forwardAllowedAfterReload() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R7", "Scout", 5, 5);
        world.addRobot(robot);
        robot.reduceShots(1);

        new InstantReloadCommand("R7").execute(world);

        String result = new ForwardCommand("R7", 1).execute(world);
        assertFalse(result.startsWith("ERROR"));
    }

    //Cannot reload beyond maximum

    @Test
    void reloadDoesNotExceedMaxShots() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();


        Robot robot = new Robot("R8", "Scout", 3, 3);
        world.addRobot(robot);


        robot.setShots(robot.getMaxShots());


        int before = robot.getShots();
        new InstantReloadCommand("R8").execute(world);

        assertEquals(before, robot.getShots());
        assertEquals(robot.getMaxShots(), robot.getShots());
    }

    //Always reloads to max, fixed time regardless of shots left

    @Test
    void reloadAlwaysRestoresToMax() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R9", "Scout", 5, 5);
        world.addRobot(robot);

        while (robot.getShots() > 0) robot.reduceShots(1);
        new InstantReloadCommand("R9").execute(world);
        assertEquals(robot.getMaxShots(), robot.getShots());
    }

    @Test
    void reloadTakesSameTimeRegardlessOfShotsLeft() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robotEmpty = new Robot("R10", "Scout", 5, 5);
        Robot robotOne   = new Robot("R11", "Scout", 5, 5);
        world.addRobot(robotEmpty);
        world.addRobot(robotOne);

        while (robotEmpty.getShots() > 0) robotEmpty.reduceShots(1);
        robotOne.reduceShots(1);

        InstantReloadCommand cmdEmpty = new InstantReloadCommand("R10");
        InstantReloadCommand cmdOne   = new InstantReloadCommand("R11");
        cmdEmpty.execute(world);
        cmdOne.execute(world);

        assertEquals(cmdEmpty.secondsReceived, cmdOne.secondsReceived);
    }

    //Error case

    @Test
    void reloadReturnsErrorForUnknownRobot() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        String result = new ReloadCommand("ghost").execute(world);
        assertTrue(result.startsWith("ERROR"));
    }

    //Return value

    @Test
    void reloadReturnsDone() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R12", "Scout", 5, 5);
        world.addRobot(robot);
        robot.reduceShots(1);

        String result = new InstantReloadCommand("R12").execute(world);
        assertEquals("Done", result);
    }

    //Repeated use

    @Test
    void canReloadMultipleTimes() {
        World world = new World(20, 5, 3, 5);
        world.clearForTesting();
        Robot robot = new Robot("R13", "Scout", 5, 5);
        world.addRobot(robot);

        while (robot.getShots() > 0) robot.reduceShots(1);
        new InstantReloadCommand("R13").execute(world);
        assertEquals(robot.getMaxShots(), robot.getShots());

        while (robot.getShots() > 0) robot.reduceShots(1);
        new InstantReloadCommand("R13").execute(world);
        assertEquals(robot.getMaxShots(), robot.getShots());
    }
}