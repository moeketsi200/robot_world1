package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.World;

public class BackCommand extends Command {

    private final int steps;

    public BackCommand(String robotName, int steps) {
        super(robotName);
        this.steps = steps;
    }

    @Override
    public String execute(World world) {
        Robot robot = findRobot(world);
        if (robot == null) {
            return "ERROR: Robot not found";
        }

        String busy = checkBusy(robot);
        if (busy != null) return busy;

        int[] delta = directionToBackDeltas(robot.getDirection());
        int moved = moveSteps(robot, world, delta, steps);

        if (moved > 0) world.printWorld();
        return moved == steps ? "Done" : "Obstructed";
    }

    /**
     * Maps the robot's facing direction to (dx, dy) movement deltas
     * for a single backward step (opposite of the facing direction).
     */
    private int[] directionToBackDeltas(Direction direction) {
        return switch (direction) {
            case NORTH -> new int[]{0,  1};
            case SOUTH -> new int[]{0, -1};
            case EAST  -> new int[]{-1, 0};
            case WEST  -> new int[]{ 1, 0};
        };
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        data.addProperty("message", executionResult);
        return data;
    }
}