package za.co.wethinkcode.robots.server.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

public abstract class Command {
    private final String robotName;

    public Command(String robotName) {
        this.robotName = robotName;
    }

    public String getRobotName() {
        return this.robotName;
    }

    public abstract String execute(World world);

    /**
     * Builds the JSON data object for a successful response.
     * @param world The world state, needed for some responses.
     * @param executionResult The string result from the execute method.
     * @return A JsonObject representing the "data" part of the response.
     */
    public abstract JsonObject buildResponse(World world, String executionResult);

    protected Robot findRobot(World world) {
        return world.getRobot(getRobotName());
    }

    /**
     * Returns an error string if the robot is busy (repairing or reloading),
     * or {@code null} if the robot is free to act.
     */
    protected String checkBusy(Robot robot) {
        if (robot.getStatus() == Status.REPAIR) {
            return "ERROR: Robot is busy repairing";
        }
        if (robot.getStatus() == Status.RELOAD) {
            return "ERROR: Robot is busy reloading";
        }
        return null;
    }

    /**
     * Moves the robot one step at a time in the given direction.
     * Stops early if the next position is invalid (wall, obstacle, or edge).
     *
     * @param robot  The robot to move.
     * @param world  The world used for position validation.
     * @param delta  Two-element array: {@code {dx, dy}} direction per step.
     * @param steps  The number of steps requested.
     * @return The number of steps actually completed before obstruction.
     */
    protected int moveSteps(Robot robot, World world, int[] delta, int steps) {
        Position pos = robot.getPosition();
        int moved = 0;
        for (int i = 0; i < steps; i++) {
            Position next = new Position(pos.getX() + delta[0], pos.getY() + delta[1]);
            if (!world.isValidPosition(next)) break;
            pos = next;
            robot.setPosition(pos);
            moved++;
        }
        return moved;
    }

    private static final Map<String, Function<JsonObject, Command>> commandFactory = new HashMap<>();

    static {
        commandFactory.put("launch", json -> {
            JsonArray args = json.get("arguments").getAsJsonArray();
            String kind = args.size() > 0 ? args.get(0).getAsString() : "normal";
            String name = json.has("robot") && !json.get("robot").getAsString().isEmpty()
                    ? json.get("robot").getAsString()
                    : (args.size() > 1 ? args.get(1).getAsString() : "Player");
            int maxShields = 3;
            int shotDistance = 3;
            if (args.size() >= 3) {
                try {
                    maxShields = Integer.parseInt(args.get(1).getAsString());
                    shotDistance = Integer.parseInt(args.get(2).getAsString());
                } catch (NumberFormatException ignored) {
                }
            }
            return new LaunchCommand(name, kind, maxShields, shotDistance);
        });
        commandFactory.put("forward", json -> new ForwardCommand(json.get("robot").getAsString(), json.get("arguments").getAsJsonArray().get(0).getAsInt()));
        commandFactory.put("back", json -> new BackCommand(json.get("robot").getAsString(), json.get("arguments").getAsJsonArray().get(0).getAsInt()));
        commandFactory.put("turn", json -> new TurnCommand(json.get("robot").getAsString(), json.get("arguments").getAsJsonArray().get(0).getAsString()));
        commandFactory.put("fire", json -> new FireCommand(json.get("robot").getAsString()));
        commandFactory.put("mine", json -> new MineCommand(json.get("robot").getAsString()));
        commandFactory.put("repair", json -> new RepairCommand(json.get("robot").getAsString()));
        commandFactory.put("reload", json -> new ReloadCommand(json.get("robot").getAsString()));
        commandFactory.put("look", json -> new LookCommand(json.get("robot").getAsString()));
        commandFactory.put("state", json -> new StateCommand(json.get("robot").getAsString()));
        commandFactory.put("quit", json -> {
            String robotName = json.has("robot") ? json.get("robot").getAsString() : "";
            return new ClientQuitCommand(robotName);
        });
    }

    public static boolean isSupported(String commandName) {
        return commandName != null && commandFactory.containsKey(commandName.toLowerCase());
    }

    public static Command create(JsonObject json) {
        String commandName = json.get("command").getAsString().toLowerCase();
        Function<JsonObject, Command> creator = commandFactory.get(commandName);
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported command: " + commandName);
        }
        return creator.apply(json);
    }
    protected Robot requireRobot(World world) {
    Robot robot = findRobot(world);

    if (robot == null) {
        throw new IllegalStateException("Robot not found");
    }

    return robot;
}
}