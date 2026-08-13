package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.world.World;

public class ClientQuitCommand extends Command {

    private final String robotName;

    public ClientQuitCommand(String robotName) {
        super(robotName);
        this.robotName = robotName;
    }

    @Override
    public String execute(World world) {
        world.removeRobot(robotName);
        return "Goodbye";
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        data.addProperty("message", executionResult);
        return data;
    }
}