package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.World;
import java.util.ArrayList;
import java.util.List;

public class LookCommand extends Command {

    public LookCommand(String robotName) {
        super(robotName);
    }

    @Override
    public String execute(World world) {
        Robot robot = findRobot(world);
        if (robot == null) {
            return "ERROR: Robot not found";
        }

        String[] directions = {"NORTH", "SOUTH", "EAST", "WEST"};
        int[][] deltas      = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};

        List<String> sightings = new ArrayList<>();
        for (int d = 0; d < directions.length; d++) {
            sightings.add(scanDirection(world, robot, directions[d], deltas[d]));
        }

        if (sightings.isEmpty()) {
            return "Nothing visible nearby";
        }
        return String.join("\n", sightings);
    }

    /**
     * Scans one direction from the robot's position up to its visibility range.
     *
     * @param dirName Human-readable direction label (e.g. "NORTH").
     * @param delta   Two-element array: {@code {dx, dy}} direction per step.
     * @return A single sighting string describing the first object seen, or
     *         "{@code <dir>: Nothing visible}" if the path is clear.
     */
    private String scanDirection(World world, Robot robot, String dirName, int[] delta) {
        Position pos = robot.getPosition();
        int visibility = robot.getShotDistance();

        for (int step = 1; step <= visibility; step++) {
            Position check = new Position(
                pos.getX() + (delta[0] * step),
                pos.getY() + (delta[1] * step)
            );

            if (!world.isValidBounds(check)) {
                return dirName + ": Edge at distance " + step;
            }
            if (world.isWallAt(check)) {
                return dirName + ": Wall at distance " + step;
            }
            if (world.isObstacleAt(check)) {
                return dirName + ": Obstacle at distance " + step;
            }
            Robot found = world.getRobotAt(check);
            if (found != null && !found.getName().equals(robot.getName())) {
                return dirName + ": Robot " + found.getName() + " at distance " + step;
            }
        }
        return dirName + ": Nothing visible";
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        JsonArray sightings = new JsonArray();
        for (String line : executionResult.split("\n")) {
            sightings.add(line);
        }
        data.add("sightings", sightings);
        return data;
    }
}