package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import za.co.wethinkcode.robots.server.config.ServerArgs;
import za.co.wethinkcode.robots.server.world.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerArgsTest {

    @Test
    void defaultValuesAreSetCorrectly() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs();

        assertEquals(5000, args.getPort());
        assertEquals(1, args.getWorldSize());
        assertEquals("none", args.getObstacle());
        assertTrue(args.getParsedObstacles().isEmpty());
    }

    @Test
    void customPortParsedCorrectly() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs("-p", "8080");

        assertEquals(8080, args.getPort());
        assertEquals(1, args.getWorldSize());
        assertEquals("none", args.getObstacle());
    }

    @Test
    void customWorldSizeParsedCorrectly() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs("-s", "100");

        assertEquals(5000, args.getPort());
        assertEquals(100, args.getWorldSize());
        assertEquals("none", args.getObstacle());
    }

    @Test
    void customObstacleParsedCorrectly() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs("-o", "10,5");

        assertEquals("10,5", args.getObstacle());
        List<Position> obstacles = args.getParsedObstacles();
        assertEquals(1, obstacles.size());
        assertEquals(new Position(10, 5), obstacles.get(0));
    }

    @Test
    void obstacleAtCoordinateOneOneParsedCorrectly() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs("-o", "1,1");

        assertEquals("1,1", args.getObstacle());
        List<Position> obstacles = args.getParsedObstacles();
        assertEquals(1, obstacles.size());
        assertEquals(new Position(1, 1), obstacles.get(0));
    }

    @Test
    void multipleArgumentsParsedCorrectly() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs("-p", "5000", "-s", "100", "-o", "10,5");

        assertEquals(5000, args.getPort());
        assertEquals(100, args.getWorldSize());
        assertEquals("10,5", args.getObstacle());
        List<Position> obstacles = args.getParsedObstacles();
        assertEquals(1, obstacles.size());
        assertEquals(new Position(10, 5), obstacles.get(0));
    }

    @Test
    void invalidObstacleFormatReturnsEmptyList() {
        ServerArgs args = new ServerArgs();
        new CommandLine(args).parseArgs("-o", "invalid");

        assertEquals("invalid", args.getObstacle());
        assertTrue(args.getParsedObstacles().isEmpty());
    }
}
