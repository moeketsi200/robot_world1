package za.co.wethinkcode.robots.server.domain;

// Robot with position, direction and state
// Brief: Model for a robot including position, direction, shields, shots and status.

public class Robot {
    private String name;
    private String kind;
    private int x;
    private int y;
    private String direction;
    private int shields;
    private int shots;
    private final int maxShields;
    private final int maxShots;
    private String status;

    // Cooldown tracking (ms since epoch)
    private long nextAllowedFireAtMillis = 0L;

    public Robot(String name, String kind, int shields, int shots) {

        this.name = name;
        this.kind = kind;
        this.shields = shields;
        this.shots = shots;
        this.maxShields = shields;
        this.maxShots = shots;
        this.x = 0;
        this.y = 0;
        this.direction = "NORTH";
        this.status = "NORMAL";
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getDirection() {
        return direction;
    }

    public int[] getPosition() {
        return new int[]{x, y};
    }

    public int getShields() {
        return shields;
    }

    public int getMaxShields() {
        return maxShields;
    }

    public int getShots() {
        return shots;
    }

    public int getMaxShots() {
        return maxShots;
    }

    public String getStatus() {
        return status;
    }

    public long getNextAllowedFireAtMillis() {
        return nextAllowedFireAtMillis;
    }

    public void setNextAllowedFireAtMillis(long nextAllowedFireAtMillis) {
        this.nextAllowedFireAtMillis = nextAllowedFireAtMillis;
    }

    public void setPosition(int x, int y) {

        this.x = x;
        this.y = y;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void updateDirection(boolean right) {
        // Update facing direction; true=right, false=left
        if (direction.equals("NORTH")) direction = right ? "EAST" : "WEST";
        else if (direction.equals("EAST")) direction = right ? "SOUTH" : "NORTH";
        else if (direction.equals("SOUTH")) direction = right ? "WEST" : "EAST";
        else if (direction.equals("WEST")) direction = right ? "NORTH" : "SOUTH";
    }

    public String updatePosition(int nrSteps, World world) {
        // Move robot by nrSteps if path is clear, return status
        int newX = x;
        int newY = y;

        for (int i = 0; i < Math.abs(nrSteps); i++) {
            int stepX = 0;
            int stepY = 0;

            int directionMultiplier = nrSteps > 0 ? 1 : -1;

            if (direction.equals("NORTH")) {
                stepY = directionMultiplier;
            } else if (direction.equals("SOUTH")) {
                stepY = -directionMultiplier;
            } else if (direction.equals("EAST")) {
                stepX = directionMultiplier;
            } else if (direction.equals("WEST")) {
                stepX = -directionMultiplier;
            }

            if (world.isPositionOpen(newX + stepX, newY + stepY)) {
                newX += stepX;
                newY += stepY;
            } else {
                this.x = newX;
                this.y = newY;
                return "Obstructed";
            }
        }
        this.x = newX;
        this.y = newY;
        return "OK";
    }

    public void fireWeapon() {
        if (this.shots > 0) {
            this.shots--;
        }
    }

    public void reloadWeapon() {
        this.shots = maxShots;
    }

    public void repairShields() {
        this.shields = maxShields;
        if (!"DEAD".equals(this.status)) {
            this.status = "NORMAL";
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void takeDamage() {
        if (this.shields > 0) {
            this.shields--;
        }
        if (this.shields == 0) {
            this.status = "DEAD";
        }
    }

    public void takeDamage(int damageAmount) {
        for (int i = 0; i < damageAmount; i++) {
            if (this.shields > 0) {
                this.shields--;
            }
        }
        if (this.shields <= 0) {
            this.shields = 0;
            this.status = "DEAD";
        }
    }

    public void restoreShields(int amount) {
        this.shields += amount;
    }

    public void setShields(int shields) {
        this.shields = shields;
        if (this.shields > 0 && "DEAD".equals(this.status)) {
            this.status = "NORMAL";
        }
    }

    public boolean isAlive() {
        return !"DEAD".equals(this.status);
    }

    @Override
    public String toString() {
         return "[" + name + "] (" + kind + ")\n" +
             "Position: [" + x + ", " + y + "]\n" +
             "Facing: " + direction + "\n" +
             "Shields: " + shields + "\n" +
             "Ammo: " + shots + " shot(s)\n" +
             "Status: " + status ;
    }
}
