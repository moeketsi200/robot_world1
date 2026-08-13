package za.co.wethinkcode.robots.server.world;
//my new world after removing movement
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.config.WorldConfig;

public class World {

    private final int width;
    private final int height;
    private final List<Position> obstacles = new ArrayList<>();
    private final Map<String, Robot> robots = new HashMap<>();
    // Map from robot name to its client handler so server can notify clients
    private final Map<String, za.co.wethinkcode.robots.server.ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final MazeGenerator maze;

    // World-level configuration: how many seconds a repair takes.
    // Defaults to 3 seconds; can be overridden via the constructor or setter.
    private int repairTime;

    private int reloadTime;

    private final int visibility;

    private final int maxShields;
    private final int maxShots;


    public World(WorldConfig config) {
        int size = config.getWorldSize();
        int adjustedSize = (size % 2 == 0) ? size + 1 : size;

        this.width = adjustedSize;
        this.height = adjustedSize;
        this.repairTime = config.getRepairTime();
        this.reloadTime = config.getReloadTime();
        this.visibility = config.getVisibility();

        this.maxShields = config.getMaxShields();
        this.maxShots = config.getMaxShots();

        this.maze = new MazeGenerator(width, height, config.getObstacleCount());

        if (config.getCustomObstacles() != null && !config.getCustomObstacles().isEmpty()) {
            maze.clearForTesting();
            this.obstacles.addAll(config.getCustomObstacles());
        } else if (size < 11) {
            maze.clearForTesting();
        } else {
            switch (config.getMazeType()) {
                case 1 -> maze.generateMaze(5);
                case 2 -> maze.generateRoomMaze(3);
                case 3 -> maze.generateMaze(8);
                default -> maze.generateMaze(3);
            }
            this.obstacles.addAll(maze.getObstacles());
        }

        maze.saveObstaclesToFile("data.txt");
        setupShutdownHook();

        System.out.println("\n=== WORLD CREATED ===");
        System.out.println("Size: " + width + "x" + height);
        System.out.println("Visibility: " + visibility);
        System.out.println("====================\n");
        printWorld();
    }

    // THATO THATO THATO Constructor accepts maze size parameters - THATO THATO THATO
    @Deprecated
    public World(int size, int mazeType, int repairTime, int reloadTime) {
        // Ensure size is odd for proper maze generation
        int adjustedSize = (size % 2 == 0) ? size + 1 : size;
        adjustedSize = Math.max(adjustedSize, 11);

        this.width = adjustedSize;
        this.height = adjustedSize;
        this.repairTime = repairTime;
        this.reloadTime = reloadTime;

        this.visibility = 5;

        this.maxShields = 5;
        this.maxShots = 5;

        this.maze = new MazeGenerator(width, height);

        // Generate maze based on type
        switch (mazeType) {
            case 1 -> maze.generateMaze(5);  // Standard maze
            case 2 -> maze.generateRoomMaze(3); // Room-based maze
            case 3 -> maze.generateMaze(8);  // Complex maze
            default -> maze.generateMaze(3);  // Simple maze
        }

        // Load obstacles from maze generator
        obstacles.addAll(maze.getObstacles());

        // Save obstacles to data.txt
        maze.saveObstaclesToFile("data.txt");

        System.out.println("\n=== WORLD CREATED ===");
        System.out.println("Size: " + width + "x" + height);
        System.out.println("Maze Type: " + mazeType);
        System.out.println("Repair Time: " + repairTime + "s");
        System.out.println("Walls: " + countWalls());
        System.out.println("Obstacles: " + obstacles.size());
        System.out.println("====================\n");

        printWorld();

        // Set up automatic cleanup when server stops
        setupShutdownHook();
    }

    private int countWalls() {
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (maze.isWall(x, y)) count++;
            }
        }
        return count;
    }

    // Set up shutdown hook to delete data.txt when server stops
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nCleaning up...");
            MazeGenerator.deleteObstaclesFile("data.txt");
        }));
    }

    //Two-arg constructor — keeps existing callers working; repair time defaults to 3s.
    @Deprecated
    public World(int size, int mazeType) {
        this(size, mazeType, 3, 5);
    }

    //Returns how many seconds a repair operation takes in this world.
    public int getRepairTime() {
        return repairTime;
    }

    public int getReloadTime() {
        return reloadTime;
    }

    // Allows tests (and future configuration code) to change the repair time.
    public void setRepairTime(int repairTime) {
        this.repairTime = repairTime;
    }


    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getVisibility() { return this.visibility; }
    public Collection<Robot> getAllRobots() { return robots.values(); }
    public List<Position> getObstacles() { return obstacles; }
    public MazeGenerator getMaze() { return maze; }

    public int getMaxShields() {
        return maxShields;
    }

    public int getMaxShots() {
        return maxShots;
    }

    public synchronized boolean addRobot(Robot robot) {
        if (robots.containsKey(robot.getName())) return false;

        robot.setShields(this.maxShields);
        robot.setShots(this.maxShots);

        for (int i = 0; i < 1000; i++) {
            Position candidate = new Position(random.nextInt(width), random.nextInt(height));
            if (isValidPosition(candidate)) {
                robot.setPosition(candidate);
                robots.put(robot.getName(), robot);
                printWorld();
                return true;
            }
        }
        return false;
    }

    public Robot getRobot(String name) {
        return robots.get(name);
    }

    public void removeRobot(String name) {
        removeRobot(name, false);
    }

    /**
     * Remove a robot. If notifyClient is true and a client handler is registered,
     * notify the client before removing.
     */
    public void removeRobot(String name, boolean notifyClient) {
        if (notifyClient) {
            za.co.wethinkcode.robots.server.ClientHandler handler = clientHandlers.remove(name);
            if (handler != null) {
                handler.notifyRemovedByServer();
            }
        } else {
            clientHandlers.remove(name);
        }

        robots.remove(name);
        printWorld();
    }

    public void registerClientHandler(String name, za.co.wethinkcode.robots.server.ClientHandler handler) {
        if (name != null && handler != null) clientHandlers.put(name, handler);
    }

    public void unregisterClientHandler(String name) {
        if (name != null) clientHandlers.remove(name);
    }

    public void removeAllRobots() {
        robots.clear();
        printWorld();
    }

    private boolean isOutOfBounds(Position pos) {
        return pos.getX() < 0 || pos.getX() >= width || pos.getY() < 0 || pos.getY() >= height;
    }

    /** Returns true if the position is within the world boundary (i.e. not out-of-bounds). */
    public boolean isValidBounds(Position pos) {
        return !isOutOfBounds(pos);
    }

    // World only provides validation - no movement logic
    public boolean isValidPosition(Position pos) {
        if (isOutOfBounds(pos)) return false;
        if (maze.isWall(pos.getX(), pos.getY())) return false;
        if (isObstacleAt(pos)) return false;
        if (getRobotAt(pos) != null) return false;
        return true;
    }

    public boolean isWallAt(Position pos) {
        if (isOutOfBounds(pos)) return true;
        return maze.isWall(pos.getX(), pos.getY());
    }

    public boolean isObstacleAt(Position pos) {
        for (Position obs : obstacles) {
            if (obs.getX() == pos.getX() && obs.getY() == pos.getY()) return true;
        }
        return false;
    }

    public Robot getRobotAt(Position pos) {
        for (Robot r : robots.values()) {
            if (r.getPosition().getX() == pos.getX() && r.getPosition().getY() == pos.getY()) return r;
        }
        return null;
    }

    // BACKDOOR FOR TESTING ONLY: Clears the maze so tests are predictable
    public void clearForTesting() {
        maze.clearForTesting();
        obstacles.clear();
    }

    public void printWorld() {
        System.out.println("\n=== WORLD STATE ===");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position cell = new Position(x, y);
                Robot robotHere = getRobotAt(cell);

                if (robotHere != null) {
                    System.out.print(getRobotSymbol(robotHere));
                    continue;
                }

                if (isObstacleAt(cell)) {
                    System.out.print("█");
                } else if (maze.isWall(x, y)) {
                    System.out.print("#");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
        System.out.println("===================\n");
    }

    private char getRobotSymbol(Robot robot) {
        return switch (robot.getDirection()) {
            case NORTH -> '^';
            case SOUTH -> 'v';
            case EAST -> '>';
            case WEST -> '<';
        };
    }
}