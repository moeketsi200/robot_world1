package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.Status;
import za.co.wethinkcode.robots.server.world.World;

/** Repair Command - restores a robot's shield to their configured maximum.
 * World configures how long a repair takes (in seconds) via getRepairTime
 * Shields are repaired to full amount (maxShields) after repair time
 * It is always the same amount of time regardless of how many hits remain
 * Shields cannot exceed configured maximum.
 * Robot cannot move while repairing
 */

public class RepairCommand extends Command {

    public RepairCommand(String robotName) {
        super(robotName);
    }

    @Override
    public String execute(World world) {
        Robot robot = findRobot(world);

        if (robot == null) {
            return "ERROR: Robot not found";
        }

        //Mark the robot as repairing - movement commands are rejected
        robot.setStatus(Status.REPAIR);

        //Robot stays in REPAIR status the entire time
        sleepForRepair(world.getRepairTime());

        //Restores shields fully - does not exceed maxShields
        robot.repairShields();

        //Repair complete - robot can move again
        robot.setStatus(Status.NORMAL);

        world.printWorld();
        return "Done";
    }

    //Sleeps for given number of seconds
    //Protected so tests can subclass and override this to avoid real delays

    protected void sleepForRepair(int seconds) {
        try {
            Thread.sleep((long) seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //restored interrupted flag
        }
    }

    @Override
    public JsonObject buildResponse(World world, String executionResult) {
        JsonObject data = new JsonObject();
        data.addProperty("message", "Done"); // The result is just "Done"
        Robot r = findRobot(world);
        if (r != null) data.addProperty("shields", r.getShields());
        return data;
    }
}
