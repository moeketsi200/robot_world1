package za.co.wethinkcode.robots.server.commands;

import com.google.gson.JsonObject;
import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.World;

/*** Fire Command - fires a bullet in the direction the robot is facing.
 * Robot must exist and have at least one shot remaining.
 * Bullet travels up to shotDistance cells in the direction the shooter is facing.
 * The bullet stops when it hits a wall or obstacle.
 * If the bullet hits another robo, that robot takes 1 damage
 * If that robot's shield drops to 0, the robot is destroyed and removed from the world
 * A robot cannot shoot itself
 * 1 shot is consumed from robots ammo regardless of whether the shot hit or not.
 * ***/

public class FireCommand extends Command {

    public FireCommand(String robotName) {
        super(robotName);
    }

    @Override
    public String execute(World world) {
        Robot shooter = findRobot(world);
        if (shooter == null) {
            return "ERROR: Can't fire you don't exist XD";
        }
        if (shooter.getShots() <= 0) {
            return "ERROR: No ammo - use reload first";
        }

        shooter.reduceShots(1);

        int[] delta = directionToDeltas(shooter.getDirection());
        String result = travelBullet(world, shooter, delta[0], delta[1]);

        world.printWorld();
        return result;
    }

    /**
     * Maps the shooter's facing direction to (dx, dy) deltas for bullet travel.
     */
    private int[] directionToDeltas(Direction direction) {
        return switch (direction) {
            case NORTH -> new int[]{0, -1};
            case SOUTH -> new int[]{0,  1};
            case EAST  -> new int[]{ 1, 0};
            case WEST  -> new int[]{-1, 0};
        };
    }

    /**
     * Steps the bullet from the shooter's position up to its maximum range.
     * Stops on a world boundary, wall, obstacle, or enemy robot hit.
     *
     * @return A result string: {@code "HIT: <name> at distance <n> [(KILLED)]"} or {@code "MISS"}.
     */
    private String travelBullet(World world, Robot shooter, int dx, int dy) {
        Position origin = shooter.getPosition();
        int maxRange = shooter.getShotDistance();

        for (int step = 1; step <= maxRange; step++) {
            Position bullet = new Position(
                origin.getX() + dx * step,
                origin.getY() + dy * step
            );

            if (world.isWallAt(bullet)) break;  // also covers out-of-bounds
            if (world.isObstacleAt(bullet)) break;

            Robot target = world.getRobotAt(bullet);
            if (target != null && !target.getName().equals(shooter.getName())) {
                return resolveHit(world, target, step);
            }
        }
        return "MISS";
    }

    /**
     * Applies damage to the target, removes it from the world if killed,
     * and returns the hit result string.
     */
    private String resolveHit(World world, Robot target, int distance) {
        target.takeDamage(1);
        boolean killed = !target.isAlive();
        if (killed) {
            world.removeRobot(target.getName());
        }
        String result = "HIT: " + target.getName() + " at distance " + distance;
        if (killed) {
            result += " (KILLED)";
        }
        return result;
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