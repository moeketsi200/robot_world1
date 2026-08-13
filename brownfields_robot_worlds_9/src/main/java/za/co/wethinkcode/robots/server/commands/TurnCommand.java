package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Robot;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;
import za.co.wethinkcode.robots.server.world.World;

public class TurnCommand extends Command {

    private final String direction;

    public TurnCommand(String robotName, String direction) {
        super(robotName);
        this.direction = direction;
    }

    @Override
    public String execute(World world) {
        Robot robot = findRobot(world);
        if (robot == null) {
            return "ERROR: Robot not found";
        }

        // Cannot move while shields are being repaired
        if (robot.getStatus() == Status.REPAIR) {
            return "ERROR: Robot is busy repairing";
        }
        if (robot.getStatus() == Status.RELOAD) {
            return "ERROR: Robot is busy reloading";
        }

        if (!direction.equalsIgnoreCase("left") && !direction.equalsIgnoreCase("right")) {
            return "ERROR: Direction must be left or right";
        }

        // Turn logic now in command
        if (direction.equalsIgnoreCase("left")) {
            robot.turnLeft();
        } else {
            robot.turnRight();
        }

        world.printWorld();
        return "Done: now facing " + robot.getDirection();
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        data.addProperty("message", executionResult);
        return data;
    }
}