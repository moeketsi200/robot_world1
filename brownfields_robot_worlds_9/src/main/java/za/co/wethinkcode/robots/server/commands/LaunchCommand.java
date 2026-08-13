package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class LaunchCommand extends Command {

    private final String kind;
    private final int maxShields;
    private final int shotDistance;

    public LaunchCommand(String robotName, String kind, int maxShields, int shotDistance) {
        super(robotName);
        this.kind         = kind;
        this.maxShields   = maxShields;
        this.shotDistance = shotDistance;
    }

    @Override
    public String execute(World world) {
        if (world.getRobot(getRobotName()) != null) {
            return "ERROR: Too many of you in this world";
        }

        Robot robot = new Robot(getRobotName(), kind, maxShields, shotDistance);

        if (!world.addRobot(robot)) {
            return "ERROR: No more space in this world";
        }

        return "OK";
    }
    
 
        @Override
    public JsonObject buildResponse(World world, String executionResult) {

        JsonObject data = new JsonObject();

        if (executionResult.startsWith("ERROR")) {
            data.addProperty("message", executionResult);
            return data;
        }

        Robot r = findRobot(world);

        JsonArray position = new JsonArray();
        position.add(r.getPosition().getX());
        position.add(r.getPosition().getY());

        data.addProperty("message", "Robot launched");
        data.add("position", position);
        data.addProperty("visibility", world.getVisibility());

        return data;
    }
}