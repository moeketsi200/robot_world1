package za.co.wethinkcode.robots.server.commands;
//modified to include mine command functionality
import com.google.gson.JsonObject;

import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class MineCommand extends Command {

    public MineCommand(String robotName) {
        super(robotName);
    }

    @Override
    public String execute(World world) {
        Robot robot = findRobot(world);
        if (robot == null) {
            return "ERROR: Robot not found";
        }

        String busy = checkBusy(robot);
        if (busy != null) {
            return busy;
        }

        world.printWorld();
        return "Done";
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        data.addProperty("message", executionResult);
        return data;
    }
}
