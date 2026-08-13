package za.co.wethinkcode.robots.server.domain;

// Brief: Base world object with position and type used in the world model.
public class WorldObject {

    private int x;
    private int y;
    private String type;
    private String owner;

    public WorldObject(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.owner = null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return type;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}