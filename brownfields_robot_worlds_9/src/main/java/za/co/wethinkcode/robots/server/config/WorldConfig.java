package za.co.wethinkcode.robots.server.config;

import za.co.wethinkcode.robots.server.world.Position;
import java.util.ArrayList;
import java.util.List;

public class WorldConfig {
    // Default values act as a fallback if the JSON file is missing fields
    private int port = 5000;
    private int worldSize = 25;
    private int mazeType = 1;
    private int repairTime = 3;
    private int reloadTime = 5;
    private int visibility = 5;
    private int maxShields = 5;
    private List<Position> customObstacles = new ArrayList<>();
    private int obstacleCount = 3;
    private int maxShots = 5;

    public int getPort() { return port; }
    public int getWorldSize() { return worldSize; }
    public int getMazeType() { return mazeType; }
    public int getRepairTime() { return repairTime; }
    public int getReloadTime() { return reloadTime; }
    public int getVisibility() { return visibility; }
    public int getMaxShields() { return maxShields; }
    public List<Position> getCustomObstacles() { return customObstacles; }
    public int getObstacleCount() { return obstacleCount ;}

    public int getMaxShots() {
        return maxShots;
    }

    public void setPort(int port) { this.port = port; }
    public void setWorldSize(int worldSize) { this.worldSize = worldSize; }
    public void setCustomObstacles(List<Position> customObstacles) { this.customObstacles = customObstacles; }
    public void setObstacleCount(int obstacleCount) { this.obstacleCount = obstacleCount; }
    public void setMazeType(int mazeType) { this.mazeType = mazeType; }
}