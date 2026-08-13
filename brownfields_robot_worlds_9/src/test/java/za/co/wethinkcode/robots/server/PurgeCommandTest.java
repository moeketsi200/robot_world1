package za.co.wethinkcode.robots.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.commands.PurgeCommand;
import za.co.wethinkcode.robots.server.config.WorldConfig;
import za.co.wethinkcode.robots.server.world.World;


public class PurgeCommandTest {

    private World world;
    private PurgeCommand purgeCommand;


    /*
        Before each test, we set up a new instance of the World and PurgeCommand classes.
        To ensures that each test starts with a clean state, preventing interference between tests.
    */
    @BeforeEach
    public void setup() {
        WorldConfig config = new WorldConfig();
        world = new World(config);
        purgeCommand = new PurgeCommand();
    }

    @Test
    public void purgeRemovesAllRobotsFromTheWorld() {

        /*
        Created four robot instances with different names and added them to the world.
        Then called the purgeRobots() method of the PurgeCommand class, passing the world instance as an argument.
        We then asserted that the size of the list of robots in the world is 0
        indicating that all robots have been successfully removed.
        
        */
        Robot robot1 = new Robot("Edison");
        Robot robot2 = new Robot("RobotBoy");
        var robot3 = new Robot("Robocop");
        var robot4 = new Robot("Terminator");

        world.addRobot(robot1);
        world.addRobot(robot2);
        world.addRobot(robot3);
        world.addRobot(robot4);

        purgeCommand.purgeRobots(world);

        assertEquals(0, world.getAllRobots().size());
    }

    @Test
    public void purgeOnEmptyWorldLeavesWorldUnaffected() {

        /*
        This test checks the behavior of the purgeRobots() method when the world is empty.
        It first asserts that the size of the list of robots in the world is 0,
        indicating that there are no robots connected. Then it calls the purgeRobots() method of the PurgeCommand class, passing the world instance as an argument.
        Finally, it asserts that the size of the list of robots in the world is still 0, confirming that the purge operation did not affect the empty world.
        Additionally, it checks that the result of the purgeRobots method is as expected.
        */
        assertEquals(0, world.getAllRobots().size());
        String result = purgeCommand.purgeRobots(world);
        purgeCommand.purgeRobots(world);
        assertEquals("No robots connected", result);
        assertEquals(0, world.getAllRobots().size());
    }

    @Test
    public void purgeRemovesEachRobotExactlyOnceWithNoRobotsLeft() {

        Robot robot1 = new Robot("Edison");
        Robot robot2 = new Robot("RobotBoy");
        Robot robot3 = new Robot("Robocop");
        Robot robot4 = new Robot("Terminator");


        world.addRobot(robot1);
        world.addRobot(robot2);
        world.addRobot(robot3);
        world.addRobot(robot4);

        purgeCommand.purgeRobots(world);

        assertEquals(0, world.getAllRobots().size());
    }
}