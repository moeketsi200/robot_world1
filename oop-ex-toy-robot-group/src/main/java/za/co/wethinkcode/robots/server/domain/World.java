package za.co.wethinkcode.robots.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

// Brief: World model that stores objects and robots and provides placement and visibility helpers.
public class World {

    private final int width;
    private final int height;

    private final ArrayList<WorldObject> objects;
    private final List<Robot> robots;
    private final List<WorldObject> mines;
    private final Random random = new Random();
    private final int visibility;
    private final int defaultShieldStrength;
    private final java.util.Map<String, Integer> reloadTimes;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.visibility = 10; // default visibility
        this.defaultShieldStrength = 5;
        this.reloadTimes = java.util.Map.of(
            "normal", 1000,
            "sniper", 3000,
            "heavy", 1500,
            "bomber", 2000
        );


        objects = new ArrayList<>();
        robots = new CopyOnWriteArrayList<>();
        mines = new CopyOnWriteArrayList<>();

        // Default obstacle
        objects.add(new WorldObject(2, 3, "Rock"));
    }

    public World(WorldConfig config) {
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.visibility = config.getVisibility();
        this.defaultShieldStrength = config.getDefaultShieldStrength();
        this.reloadTimes = config.getReloadTimes() == null ? java.util.Map.of(
            "normal", 1000,
            "sniper", 3000,
            "heavy", 1500,
            "bomber", 2000
        ) : config.getReloadTimes();

        objects = new ArrayList<>();
        robots = new CopyOnWriteArrayList<>();
        mines = new CopyOnWriteArrayList<>();

        if (config.getObstacles() != null) {
            for (WorldConfig.WorldObjectConfig objConfig : config.getObstacles()) {
                objects.add(
                    new WorldObject(
                        objConfig.getX(),
                        objConfig.getY(),
                        objConfig.getType()
                    )
                );
            }
        }
    }
//this.comment for commit
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVisibility() {
        return visibility;
    }

    public int getDefaultShieldStrength() {
        return defaultShieldStrength;
    }

    public java.util.Map<String, Integer> getReloadTimes() {
        return reloadTimes;
    }


    public ArrayList<WorldObject> getObjects() {
        return objects;
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public List<WorldObject> getMines() {
        return mines;
    }

    public void addMine(WorldObject mine) {
        mines.add(mine);
    }

    public void removeMine(WorldObject mine) {
        mines.remove(mine);
    }

    public boolean addRobot(Robot robot) {
        if (!isInsideWorld(robot.getX(), robot.getY())) {
            return false;
        }

        if (!isPositionOpen(robot.getX(), robot.getY())) {
            return false;
        }

        robots.add(robot);
        return true;
    }

    public boolean placeRobot(Robot robot) {
        int maxAttempts = 100;

        // Random placement first
        for (int i = 0; i < maxAttempts; i++) {
            int x = random.nextInt(width + 1) - width / 2;
            int y = random.nextInt(height + 1) - height / 2;

            if (isPositionOpen(x, y)) {
                robot.setPosition(x, y);
                robots.add(robot);
                return true;
            }
        }

        // Fallback: scan for an open position
        for (int y = -height / 2; y <= height / 2; y++) {
            for (int x = -width / 2; x <= width / 2; x++) {
                if (isPositionOpen(x, y)) {
                    robot.setPosition(x, y);
                    robots.add(robot);
                    return true;
                }
            }
        }

        return false;
    }

    public void removeRobot(Robot robot) {
        robots.remove(robot);
    }

    public boolean isInsideWorld(int x, int y) {
        return x >= -width / 2 && x <= width / 2
                && y >= -height / 2 && y <= height / 2;
    }

    public boolean isPositionOpen(int x, int y) {
        if (!isInsideWorld(x, y)) {
            return false;
        }

        for (WorldObject object : objects) {
            if (object.getX() == x && object.getY() == y) {
                return false;
            }
        }

        for (Robot robot : robots) {
            if (robot.getX() == x && robot.getY() == y) {
                return false;
            }
        }

        return true;
    }
}