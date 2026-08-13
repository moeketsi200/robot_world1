package za.co.wethinkcode.robots.server.world;

import java.util.*;

public class Obstacles implements Iterable<Position> {

    private final List<Position> obstacles = new ArrayList<>();
    private final Position topLeft;
    private final Position bottomRight;
    private final Random random = new Random();

    public Obstacles(Position topLeft, Position bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public void addObstacle(Position pos) {
        if (!obstacles.contains(pos) && isInside(pos)) {
            obstacles.add(pos);
        }
    }

    public boolean hasObstacle(Position pos) {
        return obstacles.contains(pos);
    }

    public List<Position> getAll() {
        return new ArrayList<>(obstacles);
    }

    public void clear() {
        obstacles.clear();
    }

    public int size() {
        return obstacles.size();
    }

    public boolean isEmpty() {
        return obstacles.isEmpty();
    }

    @Override
    public Iterator<Position> iterator() {
        return obstacles.iterator();
    }

    private boolean isInside(Position p) {
        return p.getX() > topLeft.getX()
                && p.getX() < bottomRight.getX()
                && p.getY() > topLeft.getY()
                && p.getY() < bottomRight.getY();
    }

    public void loadObstaclesFromPattern(int type) {
        clear();

        int minX = topLeft.getX() + 1;
        int maxX = bottomRight.getX() - 1;
        int minY = topLeft.getY() + 1;
        int maxY = bottomRight.getY() - 1;

        int cx = (minX + maxX) / 2;
        int cy = (minY + maxY) / 2;

        switch (type) {
            case 1 -> {
                for (int x = minX; x <= maxX; x++) {
                    addObstacle(new Position(x, cy));
                }
                for (int y = minY; y <= maxY; y++) {
                    addObstacle(new Position(cx, y));
                }
            }

            case 2 -> {
                for (int x = minX; x <= maxX; x++) {
                    addObstacle(new Position(x, minY));
                    addObstacle(new Position(x, maxY));
                }
            }

            default -> {
                for (int i = 0; i < 20; i++) {
                    int x = random.nextInt(maxX - minX + 1) + minX;
                    int y = random.nextInt(maxY - minY + 1) + minY;
                    addObstacle(new Position(x, y));
                }
            }
        }
    }
}