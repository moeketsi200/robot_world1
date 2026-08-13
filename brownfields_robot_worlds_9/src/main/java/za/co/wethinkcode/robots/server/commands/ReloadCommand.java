package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;
import za.co.wethinkcode.robots.server.world.Status;

public class ReloadCommand extends Command {

    private final String robotName;

    public ReloadCommand(String robotName) {
        super(robotName);
        this.robotName = robotName;
    }

    // @Override
    @Override
    public String execute(World world) {
        try {
            Robot robot = requireRobot(world);

            robot.setStatus(Status.RELOAD);
            sleepForReload(world.getReloadTime());
            robot.reloadShots();
            robot.setStatus(Status.NORMAL);

            return "Done";
        } catch (IllegalStateException e) {
            return "ERROR: Robot not found";
        }
    }
    // public String execute(World world) {
    //     Robot robot = world.getRobot(robotName);
    //     if (robot == null) return "ERROR: Robot not found";

    //     robot.setStatus(Status.RELOAD);
    //     sleepForReload(world.getReloadTime());
    //     robot.reloadShots();
    //     robot.setStatus(Status.NORMAL);

    //     return "Done";
    // }



    protected void sleepForReload(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        data.addProperty("message", executionResult);
        Robot r = findRobot(world);
        if (r != null) data.addProperty("shots", r.getShots());
        return data;
    }
}