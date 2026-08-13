package za.co.wethinkcode.robots.server.world;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MazeGenerator {

    private static final int BORDER_MARGIN = 3;
    private static final int SPAWN_RADIUS = 4;
    private static final int MAX_PLACEMENT_ATTEMPTS = 200;

    private final int width;
    private final int height;
    private final boolean[][] walls;
    private final List<Position> obstacles;
    private final int obstacleCount;

    public MazeGenerator(int width, int height) {
        this(width, height, 3);
    }

    public MazeGenerator(int width, int height, int obstacleCount) {
        this.width = width;
        this.height = height;

        int maxPlayableObstacles;
        if (width * height >= 1600) {
            maxPlayableObstacles = 12;
        } else if (width * height >= 900) {
            maxPlayableObstacles = 8;
        } else if (width * height >= 400) {
            maxPlayableObstacles = 5;
        } else {
            maxPlayableObstacles = 3;
        }
        this.obstacleCount = Math.min(obstacleCount, maxPlayableObstacles);

        this.walls = new boolean[width][height];
        this.obstacles = new ArrayList<>();

        System.out.println("Playability limit: Maximum " + maxPlayableObstacles + " obstacles allowed");
    }

    // =========================
    // PUBLIC API
    // =========================

    public void generateMaze(int complexity) {

        initializeOpenMaze();

        addBorderWalls();

        addInternalWalls(complexity);

        clearSpawnArea(SPAWN_RADIUS);

        placeAllObstacles();

        printMazeStatistics();
    }

    public void generateRoomMaze(int roomSize) {

        Random random = new Random();

        initializeOpenMaze();

        int roomSpacing = roomSize + 3;

        carveAllRooms(roomSize, roomSpacing, random);

        carveAllCorridors(roomSpacing);

        addBorderWalls();

        clearRoomMazeSpawnArea();

        System.out.println("Generated open room maze with " + obstacleCount + " obstacles");

        placeRoomMazeObstacles(random);

        printRoomMazeStatistics();
    }

    public boolean isWall(Position position) {

        if (isOutsideWorld(position)) {
            return true;
        }

        return walls[position.getX()][position.getY()];
    }

    public boolean isWall(int x, int y) {
        if (isOutsideBounds(x, y)) return true;
        return walls[x][y];
    }

    private boolean isOutsideBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public List<Position> getObstacles() {
        return new ArrayList<>(obstacles);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getObstacleCount() {
        return obstacleCount;
    }

    public void printMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (walls[x][y]) {
                    System.out.print("#");
                } else if (hasObstacleAt(x, y)) {
                    System.out.print("█");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
    }

    public void saveObstaclesToFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("# Maze Configuration\n");
            writer.write("# Format: width,height\n");
            writer.write(width + "," + height + "\n");
            writer.write("# Obstacle Count: " + obstacleCount + "\n");
            writer.write("# Obstacles (x,y)\n");

            for (Position obs : obstacles) {
                writer.write(obs.getX() + "," + obs.getY() + "\n");
            }

            System.out.println("Saved " + obstacles.size() + " obstacles to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving obstacles to file: " + e.getMessage());
        }
    }

    public static void deleteObstaclesFile(String filename) {
        try {
            Path filePath = Paths.get(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("Deleted " + filename);
            }
        } catch (IOException e) {
            System.out.println("Error deleting file: " + e.getMessage());
        }
    }

    public void clearForTesting() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walls[x][y] = false;
            }
        }
        obstacles.clear();
    }

    // =========================
    // MAZE INITIALIZATION
    // =========================

    private void initializeOpenMaze() {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walls[x][y] = false;
            }
        }
    }

    private void addBorderWalls() {

        for (int x = 0; x < width; x++) {
            walls[x][0] = true;
            walls[x][height - 1] = true;
        }

        for (int y = 0; y < height; y++) {
            walls[0][y] = true;
            walls[width - 1][y] = true;
        }
    }

    private void addInternalWalls(int complexity) {

        Random random = new Random();

        int targetWallPercentage = getWallPercentage(complexity);

        int totalCells = width * height;
        int targetWalls = (totalCells * targetWallPercentage) / 100;

        int wallsPlaced = countBorderWalls();

        while (wallsPlaced < targetWalls) {

            Position candidate = randomInnerPosition(random);

            if (canPlaceWall(candidate)) {

                walls[candidate.getX()][candidate.getY()] = true;
                wallsPlaced++;
            }
        }
    }

    private int getWallPercentage(int complexity) {

        switch (complexity) {
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 20;
            default:
                return 15;
        }
    }

    private int countBorderWalls() {
        return (width * 2) + (height * 2) - 4;
    }

    private Position randomInnerPosition(Random random) {

        return new Position(
                2 + random.nextInt(width - 4),
                2 + random.nextInt(height - 4)
        );
    }

    private boolean canPlaceWall(Position position) {

        return isOpenSpace(position)
                && hasEnoughOpenNeighbours(position);
    }

    private boolean hasEnoughOpenNeighbours(Position position) {

        int x = position.getX();
        int y = position.getY();

        int openNeighbours = 0;

        if (!walls[x + 1][y]) openNeighbours++;
        if (!walls[x - 1][y]) openNeighbours++;
        if (!walls[x][y + 1]) openNeighbours++;
        if (!walls[x][y - 1]) openNeighbours++;

        return openNeighbours >= 3;
    }

    // =========================
    // ROOM MAZE - ROOM CARVING
    // =========================

    private void carveAllRooms(int roomSize, int roomSpacing, Random random) {

        for (int x = 2; x < width - roomSize; x += roomSpacing) {
            for (int y = 2; y < height - roomSize; y += roomSpacing) {
                carveRoom(new Position(x, y), roomSize, random);
            }
        }
    }

    private void carveRoom(Position origin, int roomSize, Random random) {

        int actualRoomSize = roomSize + 2;

        clearRoomArea(origin, actualRoomSize);

        maybeAddInteriorWall(origin, actualRoomSize, roomSize, random);
    }

    private void clearRoomArea(Position origin, int actualRoomSize) {

        int x = origin.getX();
        int y = origin.getY();

        for (int rx = 0; rx < actualRoomSize; rx++) {
            for (int ry = 0; ry < actualRoomSize; ry++) {
                if (x + rx < width - 1 && y + ry < height - 1) {
                    walls[x + rx][y + ry] = false;
                }
            }
        }
    }

    private void maybeAddInteriorWall(Position origin, int actualRoomSize, int roomSize, Random random) {

        boolean shouldAddWall = random.nextInt(20) == 0 && roomSize > 2;

        if (!shouldAddWall) {
            return;
        }

        int wallX = origin.getX() + 1 + random.nextInt(actualRoomSize - 2);
        int wallY = origin.getY() + 1 + random.nextInt(actualRoomSize - 2);
        walls[wallX][wallY] = true;
    }

    // =========================
    // ROOM MAZE - CORRIDORS
    // =========================

    private void carveAllCorridors(int roomSpacing) {

        carveVerticalCorridors(roomSpacing);

        carveHorizontalCorridors(roomSpacing);
    }

    private void carveVerticalCorridors(int roomSpacing) {

        for (int x = roomSpacing; x < width; x += roomSpacing) {
            for (int y = 0; y < height; y++) {
                carveVerticalCorridorCell(x, y);
            }
        }
    }

    private void carveVerticalCorridorCell(int x, int y) {

        if (x <= 0 || x >= width - 1) {
            return;
        }

        walls[x][y] = false;
        if (x + 1 < width - 1) walls[x + 1][y] = false;
        if (x - 1 > 0) walls[x - 1][y] = false;
    }

    private void carveHorizontalCorridors(int roomSpacing) {

        for (int y = roomSpacing; y < height; y += roomSpacing) {
            for (int x = 0; x < width; x++) {
                carveHorizontalCorridorCell(x, y);
            }
        }
    }

    private void carveHorizontalCorridorCell(int x, int y) {

        if (y <= 0 || y >= height - 1) {
            return;
        }

        walls[x][y] = false;
        if (y + 1 < height - 1) walls[x][y + 1] = false;
        if (y - 1 > 0) walls[x][y - 1] = false;
    }

    // =========================
    // ROOM MAZE - SPAWN AREA
    // =========================

    private void clearRoomMazeSpawnArea() {

        int centerX = width / 2;
        int centerY = height / 2;

        for (int x = centerX - 5; x <= centerX + 5; x++) {
            for (int y = centerY - 5; y <= centerY + 5; y++) {
                if (isWithinRoomMazeSpawnBounds(x, y)) {
                    walls[x][y] = false;
                }
            }
        }
    }

    private boolean isWithinRoomMazeSpawnBounds(int x, int y) {
        return x > 0 && x < width - 1 && y > 0 && y < height - 1;
    }

    // =========================
    // ROOM MAZE - OBSTACLES
    // =========================

    private void placeRoomMazeObstacles(Random random) {

        int maxAttempts = obstacleCount * MAX_PLACEMENT_ATTEMPTS;

        for (int i = 0; i < obstacleCount; i++) {
            tryPlaceRoomMazeObstacle(random, maxAttempts);
        }
    }

    private boolean tryPlaceRoomMazeObstacle(Random random, int maxAttempts) {

        int attempts = 0;

        while (attempts < maxAttempts) {

            Position candidate = randomPosition(random);

            if (canPlaceRoomMazeObstacle(candidate)) {
                obstacles.add(candidate);
                return true;
            }

            attempts++;
        }

        return false;
    }

    private boolean canPlaceRoomMazeObstacle(Position position) {

        return isOpenSpace(position)
                && isInsideRoomMazePlayableArea(position)
                && isAvailable(position)
                && !isNearRoomMazeCentre(position);
    }

    private boolean isInsideRoomMazePlayableArea(Position position) {

        int x = position.getX();
        int y = position.getY();

        return x > 3 && x < width - 4
                && y > 3 && y < height - 4;
    }

    private boolean isNearRoomMazeCentre(Position position) {

        int centerX = width / 2;
        int centerY = height / 2;

        int distanceX = Math.abs(position.getX() - centerX);
        int distanceY = Math.abs(position.getY() - centerY);

        return distanceX < 5 && distanceY < 5;
    }

    // =========================
    // ROOM MAZE - REPORTING
    // =========================

    private void printRoomMazeStatistics() {

        System.out.println("Successfully placed " + obstacles.size() + " obstacles in open room maze");
        System.out.println(
                "Final wall count: "
                        + countWalls()
                        + " ("
                        + (countWalls() * 100 / (width * height))
                        + "% of world)"
        );
    }

    // =========================
    // SPAWN AREA (STANDARD MAZE)
    // =========================

    private void clearSpawnArea(int radius) {

        Position centre = getCentre();

        for (int x = centre.getX() - radius;
             x <= centre.getX() + radius;
             x++) {

            for (int y = centre.getY() - radius;
                 y <= centre.getY() + radius;
                 y++) {

                Position current = new Position(x, y);

                if (isInsideWorld(current)) {
                    walls[x][y] = false;
                }
            }
        }
    }

    private Position getCentre() {

        return new Position(width / 2, height / 2);
    }

    // =========================
    // OBSTACLE PLACEMENT (STANDARD MAZE)
    // =========================

    private void placeAllObstacles() {

        Random random = new Random();
        int maxAttempts = obstacleCount * MAX_PLACEMENT_ATTEMPTS;

        for (int i = 0; i < obstacleCount; i++) {
            tryPlaceOneObstacle(random, maxAttempts);
        }
    }

    private boolean tryPlaceOneObstacle(Random random, int maxAttempts) {

        int attempts = 0;

        while (attempts < maxAttempts) {

            Position candidate = randomPosition(random);

            if (canPlaceObstacle(candidate)) {

                placeObstacle(candidate);
                return true;
            }

            attempts++;
        }

        return false;
    }

    private Position randomPosition(Random random) {

        return new Position(
                random.nextInt(width),
                random.nextInt(height)
        );
    }

    private boolean canPlaceObstacle(Position position) {

        return isOpenSpace(position)
                && isInsidePlayableArea(position)
                && isAvailable(position)
                && isOutsideSpawnArea(position);
    }

    private boolean isOpenSpace(Position position) {

        return !walls[position.getX()][position.getY()];
    }

    private boolean isInsidePlayableArea(Position position) {

        int x = position.getX();
        int y = position.getY();

        return x > BORDER_MARGIN
                && x < width - BORDER_MARGIN - 1
                && y > BORDER_MARGIN
                && y < height - BORDER_MARGIN - 1;
    }

    private boolean isAvailable(Position position) {

        return !hasObstacleAt(position);
    }

    private boolean isOutsideSpawnArea(Position position) {

        return !isInsideSpawnArea(position);
    }

    private boolean isInsideSpawnArea(Position position) {

        Position centre = getCentre();

        int distanceX = Math.abs(position.getX() - centre.getX());
        int distanceY = Math.abs(position.getY() - centre.getY());

        return distanceX < SPAWN_RADIUS
                && distanceY < SPAWN_RADIUS;
    }

    private void placeObstacle(Position position) {

        obstacles.add(position);
    }

    private boolean hasObstacleAt(Position position) {
        return hasObstacleAt(position.getX(), position.getY());
    }

    private boolean hasObstacleAt(int x, int y) {
        for (Position obstacle : obstacles) {
            if (obstacle.getX() == x && obstacle.getY() == y) {
                return true;
            }
        }
        return false;
    }

    // =========================
    // WORLD BOUNDS
    // =========================

    private boolean isInsideWorld(Position position) {

        int x = position.getX();
        int y = position.getY();

        boolean insideX = x > 0 && x < width - 1;
        boolean insideY = y > 0 && y < height - 1;

        return insideX && insideY;
    }

    private boolean isOutsideWorld(Position position) {

        return !isInsideWorld(position);
    }

    // =========================
    // REPORTING (STANDARD MAZE)
    // =========================

    private void printMazeStatistics() {

        int totalCells = width * height;
        int wallCount = countWalls();

        System.out.println(
                "Final wall count: "
                        + wallCount
                        + " ("
                        + (wallCount * 100 / totalCells)
                        + "% of world)"
        );

        System.out.println(
                "Successfully placed "
                        + obstacles.size()
                        + " obstacles"
        );
    }

    private int countWalls() {

        int count = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                if (walls[x][y]) {
                    count++;
                }
            }
        }

        return count;
    }
}