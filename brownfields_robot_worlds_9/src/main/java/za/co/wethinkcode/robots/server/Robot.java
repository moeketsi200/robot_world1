package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.world.Position;
import za.co.wethinkcode.robots.server.world.Direction;
import za.co.wethinkcode.robots.server.world.Status;

/**
 * Represents a robot in the Robot World game.
 * Every player controls one robot — it moves, shoots and takes damage.
 *
 * @author Benita Nnabuife
 * @version 2.0
 */
public class Robot {

    private String name;
    private Position position;
    private Direction direction;
    private int shots;
    private int maxShields;
    private String kind;
    private int shotDistance;
    private int shields;
    private Status status;
    private int maxShots;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    /** Quick constructor for testing — uses default values. */
    public Robot(String name) {
        this(name, "Default", 3, 3);
    }

    /**
     * Main constructor used when a player launches into the world.
     * Robot always starts at (0,0) facing NORTH with full shields and ammo.
     *
     * Shot formula from spec table: shots = 6 - shotDistance
     * Exception: shotDistance 0 means no weapon, so shots = 0
     */
    public Robot(String name, String kind, int maxShields, int shotDistance) {
        this.name = name;
        this.kind = kind;
        this.maxShields = maxShields;
        this.shotDistance = shotDistance;

        // Always starts at origin facing north — world places it properly after
        this.position = new Position(0, 0);
        this.direction = Direction.NORTH;

        // Further the shot travels, fewer bullets you get — keeps the game fair
        if (shotDistance == 0) {
            this.shots = 0;
        } else {
            this.shots = 6 - shotDistance;
        }

        // maxShots never changes — it's what shots resets to on reload
        this.maxShots = this.shots;

        // Full health, ready to go
        this.shields = maxShields;
        this.status = Status.NORMAL;
    }

    // ─────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────

    /** @return the robot's unique name */
    public String getName() { return name; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getShields() { return shields; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public Direction getDirection() { return direction; }
    public int getShots() { return shots; }
    public int getMaxShields() { return maxShields; }
    public String getKind() { return kind; }


    /** @return the maximum shots this robot can hold */
    public int getMaxShots() { return maxShots; }
//
    /** @return how far bullets travel in steps */
   public int getShotDistance() { return shotDistance; }


    // ─────────────────────────────────────────
    // Setters
    // ─────────────────────────────────────────

    /** Called by World after validating a move. */
//    public void setPosition(Position position) {
//        this.position = position;
//    }

    /** Directly sets shot count — used by server when needed. */
    public void setShots(int shots) {
        this.shots = shots;
    }

    /**
     * Sets the maximum shields allowed by the world configuration,
     * and spawns the robot with full health.
     */
    public void setShields(int newShields) {
        // Only update current health, leave maxShields alone!
        this.shields = newShields;
    }



    // ─────────────────────────────────────────
    // Health and Shields
    // ─────────────────────────────────────────

    /**
     * Deals damage to shields.
     * If shields hit zero the robot dies.
     */
    public void takeDamage(int amount) {
        reduceShield(amount);
    }

    /** Applies shield damage — shields can never go below zero. */
    public void reduceShield(int amount) {
        this.shields -= amount;
        if (this.shields <= 0) {
            this.shields = 0;
            this.status = Status.DEAD;
        }
    }

    /** Restores shields to maximum after repair time is complete. */
    public void repairShields() {
        this.shields = maxShields;
        this.status = Status.NORMAL;
    }

    /** True if the robot is still in the game. */
    public boolean isAlive() {
        return status != Status.DEAD;
    }

    // ─────────────────────────────────────────
    // Shots
    // ─────────────────────────────────────────

    /** Reduces shots — cannot go below zero. */
    public void reduceShots(int amount) {
        this.shots -= amount;
        if (this.shots < 0) {
            this.shots = 0;
        }
    }

    /** Reloads weapon back to maximum after reload time is complete. */
    public void reloadShots() {
        this.shots = this.maxShots;
    }

    // ─────────────────────────────────────────
    // Movement
    // ─────────────────────────────────────────

    /** Rotates 90 degrees left — NORTH becomes WEST, WEST becomes SOUTH, etc. */
    public void turnLeft() {
        switch (direction) {
            case NORTH -> direction = Direction.WEST;
            case WEST  -> direction = Direction.SOUTH;
            case SOUTH -> direction = Direction.EAST;
            case EAST  -> direction = Direction.NORTH;
        }
    }

    /** Rotates 90 degrees right — NORTH becomes EAST, EAST becomes SOUTH, etc. */
    public void turnRight() {
        switch (direction) {
            case NORTH -> direction = Direction.EAST;
            case EAST  -> direction = Direction.SOUTH;
            case SOUTH -> direction = Direction.WEST;
            case WEST  -> direction = Direction.NORTH;
        }
    }

    // ─────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────

    /** Shows full robot state — used by dump and robots server commands. */
    @Override
    public String toString() {
        return name + " at (" + position.getX() + "," + position.getY() + ")" +
                " facing " + direction +
                " shields=" + shields + "/" + maxShields +
                " shots=" + shots + "/" + maxShots +
                " status=" + status;
    }
}