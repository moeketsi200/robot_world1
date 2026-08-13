package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class StateCommand extends Command {

    public StateCommand(String robotName) {
        super(robotName);
    }

    @Override
    public String execute(World world) {
        Robot robot = findRobot(world);
        if (robot == null) {
            return "ERROR: Robot not found";
        }

        return robot.getPosition().getX() + "," + robot.getPosition().getY()
                + "|" + robot.getDirection()
                + "|" + robot.getShields()
                + "|" + robot.getShots()
                + "|" + robot.getStatus();
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        Robot robot = findRobot(world);
        JsonObject data = new JsonObject();

        JsonArray position = new JsonArray();
        position.add(robot.getPosition().getX());
        position.add(robot.getPosition().getY());

        data.add("position", position);
        data.addProperty("direction", robot.getDirection().toString());
        data.addProperty("shields", robot.getShields());
        data.addProperty("shots", robot.getShots());
        data.addProperty("status", robot.getStatus().toString());
        return data;
    }
}
