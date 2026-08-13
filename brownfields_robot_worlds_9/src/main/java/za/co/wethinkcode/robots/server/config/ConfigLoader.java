package za.co.wethinkcode.robots.server.config;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine;
import za.co.wethinkcode.robots.server.world.Position;

public class ConfigLoader {

    public static WorldConfig loadConfig(String filePath) {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, WorldConfig.class);
        } catch (IOException e) {
            System.out.println("Warning: 'config.json' not found or unreadable. Using default server settings.");
            return new WorldConfig(); // Returns defaults
        }
    }

    public static WorldConfig loadConfig(String filePath, String[] cliArgs) {
        WorldConfig config = loadConfig(filePath);
        CliArgs overrides = CommandLine.populateCommand(new CliArgs(), cliArgs);
        applyOverrides(config, overrides);
        return config;
    }

    private static void applyOverrides(WorldConfig config, CliArgs overrides) {
        if (overrides.getPort() != null) {
            config.setPort(overrides.getPort());
        }
        if (overrides.getWorldSize() != null) {
            config.setWorldSize(overrides.getWorldSize());
        }
        if (overrides.getObstacle() != null) {
            config.setCustomObstacles(parseObstacle(overrides.getObstacle()));
        }
    }

    private static List<Position> parseObstacle(String raw) {
        if (raw.equalsIgnoreCase("none")) {
            return new ArrayList<>();
        }
        String[] parts = raw.split(",");
        List<Position> obstacles = new ArrayList<>();
        obstacles.add(new Position(
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        ));
        return obstacles;
    }
}