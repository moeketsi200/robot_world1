package za.co.wethinkcode.robots.server.config;

import picocli.CommandLine.Option;
import za.co.wethinkcode.robots.server.world.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Command line arguments configuration for Robot World Server.
 */
public class ServerArgs {

    @Option(names = {"-p"}, description = "Server Port (0..9999)", defaultValue = "5000")
    private int port = 5000;

    @Option(names = {"-s"}, description = "Size of world (1..9999)", defaultValue = "1")
    private int worldSize = 1;

    @Option(names = {"-o"}, description = "Obstacle position: x,y OR none", defaultValue = "none")
    private String obstacle = "none";

    public int getPort() {
        return port;
    }

    public int getWorldSize() {
        return worldSize;
    }

    public String getObstacle() {
        return obstacle;
    }

    public List<Position> getParsedObstacles() {
        List<Position> list = new ArrayList<>();
        if (hasCustomObstacle()) {
            String[] parts = obstacle.split(",");
            if (parts.length == 2) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    list.add(new Position(x, y));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return list;
    }

    private boolean hasCustomObstacle() {
        return obstacle != null && !obstacle.isBlank() && !obstacle.equalsIgnoreCase("none");
    }
}
