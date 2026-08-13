package za.co.wethinkcode.robots.server;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.co.wethinkcode.robots.server.config.ConfigLoader;
import za.co.wethinkcode.robots.server.config.WorldConfig;

class ConfigLoaderTest {

    @Test
    void loadsConfigFromValidJsonFile(@TempDir Path tempDir) throws Exception {
        // 1. Create a temporary config.json with some custom values
        Path configFile = tempDir.resolve("config.json");
        String jsonContent = "{\n" +
                "  \"port\": 9000,\n" +
                "  \"worldSize\": 40,\n" +
                "  \"visibility\": 10\n" +
                "}";
        Files.writeString(configFile, jsonContent);

        // 2. Load it!
        WorldConfig config = ConfigLoader.loadConfig(configFile.toAbsolutePath().toString());

        // 3. Verify it used our custom values instead of the defaults
        assertEquals(9000, config.getPort());
        assertEquals(40, config.getWorldSize());
        assertEquals(10, config.getVisibility());

        // Ensure values we didn't include in the JSON safely fell back to default
        assertEquals(3, config.getRepairTime());
    }

    @Test
    void fallsBackToDefaultsWhenFileNotFound() {
        // 1. Give it a garbage file path
        WorldConfig config = ConfigLoader.loadConfig("this_file_definitely_does_not_exist.json");

        // 2. It should safely catch the IOException and return the default config
        assertNotNull(config);
        assertEquals(5000, config.getPort());     // 5000 is the default
        assertEquals(25, config.getWorldSize()); // 25 is the default
    }
}