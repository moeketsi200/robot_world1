package za.co.wethinkcode.robots.acceptencetest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.databind.JsonNode;

import za.co.wethinkcode.robots.RobotWorldClient;
import za.co.wethinkcode.robots.RobotWorldJsonClient;
import za.co.wethinkcode.robots.server.ClientHandler;
import za.co.wethinkcode.robots.server.commands.PurgeCommand;
import za.co.wethinkcode.robots.server.config.WorldConfig;
import za.co.wethinkcode.robots.server.world.World;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PurgeCommandTest {

    /*
    As a server admin
     I want to be able to purge all robots from the world so that I can reset the  world and clear all connected clients
     Given that I am connected to the server
     When I issue the purge command
     Then all robots should be removed from the world and all connected clients should be notified that they have been removed  
     When the purge command is triggered,
    the server should remove all robots from the world
    and notify all connected clients that they have been removed
    Then all robots should be removed from the world and all connected clients should be notified that they have been removed
     
     */


    private static final int TEST_PORT = 6001; 
    private static final String DEFAULT_IP = "localhost";

    private static ServerSocket serverSocket;
    private static World world;
    private static Thread acceptThread;

    @BeforeAll
    static void startTestServer() throws IOException {
        WorldConfig config = new WorldConfig();
        world = new World(config);
        serverSocket = new ServerSocket(TEST_PORT);

        acceptThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientHandler(socket, world)).start();
                } catch (IOException e) {
                    break; // serverSocket was closed during shutdown
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    @AfterAll
    static void stopTestServer() throws IOException {
        serverSocket.close();
    }

    private RobotWorldClient newClient() {
        RobotWorldClient client = new RobotWorldJsonClient();
        client.connect(DEFAULT_IP, TEST_PORT);
        return client;
    }

    private JsonNode launch(RobotWorldClient client, String robotName) {
        String request = "{" +
                "\"robot\": \"" + robotName + "\"," +
                "\"command\": \"launch\"," +
                "\"arguments\": [\"shooter\",\"5\",\"5\"]" +
                "}";
        return client.sendRequest(request);
    }

    @Test
    @Order(1)
    void purgeOnEmptyWorldReturnsNoRobotsConnectedMessage() {


        //Given that the world is empty
        assertEquals(0, world.getAllRobots().size());

        //When the purge command runs on the empty world
        String result = new PurgeCommand().purgeRobots(world);

        
        //Then the result should be "No robots connected" and the world should still be empty
        assertEquals("No robots connected", result);
        assertEquals(0, world.getAllRobots().size());
    }

    @Test
    @Order(2)
    void purgeRemovesAllRobotsAndNotifiesEachClient() {

        //Given that there are two robots connected to the world
        RobotWorldClient client1 = newClient();
        RobotWorldClient client2 = newClient();
        assertEquals("OK", launch(client1, "Edison").get("result").asText());
        assertEquals("OK", launch(client2, "Terminator").get("result").asText());
        assertEquals(2, world.getAllRobots().size());

        //When the purge command runs on the world with two robots connected
        new PurgeCommand().purgeRobots(world);

        //Then the world should be empty and both clients should receive a notification that they have been removed
        assertEquals(0, world.getAllRobots().size());

       
        JsonNode notice1 = client1.notifyRemoved();
        JsonNode notice2 = client2.readPushedNotification();

        assertEquals("ERROR", notice1.get("result").asText());
        assertTrue(notice1.get("data").get("message").asText().toLowerCase().contains("removed from the server"));

        assertEquals("ERROR", notice2.get("result").asText());
        assertTrue(notice2.get("data").get("message").asText().toLowerCase().contains("removed from the server"));
    }

    @Test
    @Order(3)
    void purgeFreesUpRobotNamesForReuse() {
        RobotWorldClient client1 = newClient();
        RobotWorldClient client2 = newClient();


     //Given that Edison robot is already connected to the world
        assertEquals("OK", launch(client1, "Edison").get("result").asText());

       //When the purge command runs on the world with Edison robot connected
        new PurgeCommand().purgeRobots(world);
        client1.readPushedNotification(); 

      //Then a new robot with the same name "Edison" should be able to connect to the world after the purge
        assertEquals("OK", launch(client2, "Edison").get("result").asText());

  
        new PurgeCommand().purgeRobots(world);
        client2.readPushedNotification();
    }
}