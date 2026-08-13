package za.co.wethinkcode.robots.server.domain;

import java.util.List;

// Brief: Configuration holder for world parameters (size, visibility, obstacles).
public class WorldConfig {
    private int width;
    private int height;

    // Visibility range for the world (used by look command / line-of-sight rules)
    private int visibilityRange = 10;

    // Default shield strength applied to robots when shields are not explicitly provided.
    private int defaultShieldStrength = 5;

    // Weapon reload times per weapon kind.
    // Example: { "normal": 1000, "sniper": 3000, "heavy": 1500, "bomber": 2000 }
    private java.util.Map<String, Integer> reloadTimes;

    private List<WorldObjectConfig> obstacles;


    public WorldConfig() {}

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // Backward-compatible getter (old name used by existing code)
    public int getVisibility() {
        return visibilityRange;
    }

    // Backward-compatible setter (old name used by existing code)
    public void setVisibility(int visibility) {
        this.visibilityRange = visibility;
    }

    public int getVisibilityRange() {
        return visibilityRange;
    }

    public void setVisibilityRange(int visibilityRange) {
        this.visibilityRange = visibilityRange;
    }

    public int getDefaultShieldStrength() {
        return defaultShieldStrength;
    }

    public void setDefaultShieldStrength(int defaultShieldStrength) {
        this.defaultShieldStrength = defaultShieldStrength;
    }

    public java.util.Map<String, Integer> getReloadTimes() {
        return reloadTimes;
    }

    public void setReloadTimes(java.util.Map<String, Integer> reloadTimes) {
        this.reloadTimes = reloadTimes;
    }


    public List<WorldObjectConfig> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<WorldObjectConfig> obstacles) {
        this.obstacles = obstacles;
    }

    public static class WorldObjectConfig {
        private int x;
        private int y;
        private String type;

        public WorldObjectConfig() {}

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
