package za.co.wethinkcode.robots.server.commands;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MineCommandTest {

    @Test
    void executeReturnsDoneForExistingRobot() {
        World world = new World(5, 1);
        Robot robot = new Robot("miner", "Scout", 3, 3);
        world.addRobot(robot);

        Command command = new MineCommand("miner");

        assertEquals("Done", command.execute(world));
    }

    @Test
    void buildResponseIncludesAMessage() {
        World world = new World(5, 1);
        Robot robot = new Robot("miner", "Scout", 3, 3);
        world.addRobot(robot);

        Command command = new MineCommand("miner");
        var response = command.buildResponse(world, "Done");

        assertNotNull(response);
        assertEquals("Done", response.get("message").getAsString());
    }
}
