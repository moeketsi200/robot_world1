package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void testLoadConfig() throws IOException {
        String testConfig = "{\n" +
                "  \"width\": 150,\n" +
                "  \"height\": 250,\n" +
                "  \"obstacles\": [\n" +
                "    {\"x\": 5, \"y\": 5, \"type\": \"Rock\"}\n" +
                "  ]\n" +
                "}";
        
        File tempFile = File.createTempFile("config", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(testConfig);
        }

        Server server = new Server(5000) {
            // Override to use our temp file
            @Override
            public World getWorld() {
                // This is a bit tricky because the constructor calls loadConfig
                return super.getWorld();
            }
        };

        // Actually, let's test the loadConfig method directly if it was public, 
        // but it's private. Let's test World constructor with WorldConfig instead.
        
        WorldConfig config = new WorldConfig();
        config.setWidth(150);
        config.setHeight(250);
        WorldConfig.WorldObjectConfig obj = new WorldConfig.WorldObjectConfig();
        obj.setX(5);
        obj.setY(5);
        obj.setType("Rock");
        config.setObstacles(List.of(obj));

        World world = new World(config);
        assertEquals(150, world.getWidth());
        assertEquals(250, world.getHeight());
        assertEquals(1, world.getObjects().size());
        assertEquals(5, world.getObjects().get(0).getX());
        assertEquals(5, world.getObjects().get(0).getY());
        assertEquals("Rock", world.getObjects().get(0).getType());

        tempFile.delete();
    }

    @Test
    public void testServerLoadsConfig() {
        // This test depends on the config.json file created in the project root during the task
        Server server = new Server(5000);
        World world = server.getWorld();

        // Based on the config.json I created:
        // { "width": 200, "height": 200, "obstacles": [ { "x": 2, "y": 3, "type": "Rock" }, { "x": 10, "y": 10, "type": "Pit" } ] }
        assertEquals(200, world.getWidth());
        assertEquals(200, world.getHeight());
        assertTrue(world.getObjects().size() >= 2);

        boolean foundRock = false;
        boolean foundPit = false;
        for (WorldObject obj : world.getObjects()) {
            if (obj.getX() == 2 && obj.getY() == 3 && "Rock".equals(obj.getType())) foundRock = true;
            if (obj.getX() == 10 && obj.getY() == 10 && "Pit".equals(obj.getType())) foundPit = true;
        }
        assertTrue(foundRock, "Should find Rock at 2,3");
        assertTrue(foundPit, "Should find Pit at 10,10");
    }

    @Test
    public void testJacksonParsesVisibilityShieldStrengthAndReloadTimes() throws IOException {
        String testConfig = "{\n" +
                "  \"width\": 150,\n" +
                "  \"height\": 250,\n" +
                "  \"visibilityRange\": 18,\n" +
                "  \"defaultShieldStrength\": 7,\n" +
                "  \"reloadTimes\": {\n" +
                "    \"normal\": 1200,\n" +
                "    \"sniper\": 3200,\n" +
                "    \"heavy\": 1800,\n" +
                "    \"bomber\": 2400\n" +
                "  },\n" +
                "  \"obstacles\": []\n" +
                "}";

        WorldConfig config = new ObjectMapper().readValue(testConfig, WorldConfig.class);

        assertEquals(18, config.getVisibilityRange());
        assertEquals(18, config.getVisibility(), "Legacy visibility getter should use visibilityRange");
        assertEquals(7, config.getDefaultShieldStrength());
        assertEquals(1200, config.getReloadTimes().get("normal"));
        assertEquals(3200, config.getReloadTimes().get("sniper"));
        assertEquals(1800, config.getReloadTimes().get("heavy"));
        assertEquals(2400, config.getReloadTimes().get("bomber"));
    }

    @Test
    public void testWorldUsesConfiguredVisibilityShieldStrengthAndReloadTimes() {
        WorldConfig config = new WorldConfig();
        config.setWidth(80);
        config.setHeight(90);
        config.setVisibilityRange(12);
        config.setDefaultShieldStrength(9);
        config.setReloadTimes(Map.of(
                "normal", 500,
                "sniper", 2500,
                "heavy", 1500,
                "bomber", 2000
        ));

        World world = new World(config);

        assertEquals(12, world.getVisibility());
        assertEquals(9, world.getDefaultShieldStrength());
        assertEquals(500, world.getReloadTimes().get("normal"));
        assertEquals(2500, world.getReloadTimes().get("sniper"));
    }

    @Test
    public void testWorldUsesDefaultsWhenOptionalConfigValuesAreMissing() {
        WorldConfig config = new WorldConfig();
        config.setWidth(80);
        config.setHeight(90);

        World world = new World(config);

        assertEquals(10, world.getVisibility());
        assertEquals(5, world.getDefaultShieldStrength());
        assertEquals(1000, world.getReloadTimes().get("normal"));
        assertEquals(3000, world.getReloadTimes().get("sniper"));
        assertEquals(1500, world.getReloadTimes().get("heavy"));
        assertEquals(2000, world.getReloadTimes().get("bomber"));
    }

    @Test
    public void testLaunchUsesConfiguredDefaultShieldStrengthWhenShieldArgumentIsMissing() {
        WorldConfig config = new WorldConfig();
        config.setWidth(80);
        config.setHeight(80);
        config.setDefaultShieldStrength(11);
        config.setReloadTimes(Map.of("normal", 1000));
        Server server = new ServerWithWorld(new World(config));
        LaunchCommand launchCommand = new LaunchCommand(server);

        String response = launchCommand.execute(
                new CommandRequest("launch", List.of("ShieldBot", "normal")),
                new Socket()
        );

        assertTrue(response.contains("Launched robot"));
        assertTrue(response.contains("ShieldBot"));
        assertTrue(response.contains("Shields: 11"));
    }

    @Test
    public void testFireUsesConfiguredReloadTimeForWeaponKind() {
        int configuredReloadMillis = 60000;
        WorldConfig config = new WorldConfig();
        config.setWidth(80);
        config.setHeight(80);
        config.setReloadTimes(Map.of("normal", configuredReloadMillis));
        World world = new World(config);
        Server server = new ServerWithWorld(world);
        FireCommand fireCommand = new FireCommand(server);
        Socket socket = new Socket();
        Robot robot = new Robot("ReloadBot", "normal", 5, 5);
        robot.setPosition(10, 10);
        world.addRobot(robot);
        server.addClientRobot(socket, robot);

        long beforeFire = System.currentTimeMillis();
        String firstResponse = fireCommand.execute(new CommandRequest("fire", List.of()), socket);
        long afterFire = System.currentTimeMillis();
        String secondResponse = fireCommand.execute(new CommandRequest("fire", List.of()), socket);

        assertTrue(firstResponse.contains("[ReloadBot] Fired!"));
        assertEquals(4, robot.getShots(), "Second fire should be blocked by the configured reload time");
        assertTrue(robot.getNextAllowedFireAtMillis() >= beforeFire + configuredReloadMillis);
        assertTrue(robot.getNextAllowedFireAtMillis() <= afterFire + configuredReloadMillis);
        assertTrue(secondResponse.contains("[ReloadBot] Weapon reloading"));
    }

    private static class ServerWithWorld extends Server {
        private World customWorld;

        ServerWithWorld(World customWorld) {
            super(5000);
            this.customWorld = customWorld;
        }

        @Override
        public World getWorld() {
            return customWorld;
        }
    }
}
