package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;


import java.lang.reflect.Method;
import java.net.Socket;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


@DisplayName("Server Tests")
public class ServerTest {


   private static final int DEFAULT_PORT = 5000;
   private static final int DEFAULT_SHIELDS = 5;
   private static final int DEFAULT_SHOTS = 5;


   @Mock
   private Socket mockSocket;


   private Server server;


   @BeforeEach
   public void setUp() {
       MockitoAnnotations.openMocks(this);
       server = new Server(DEFAULT_PORT);
   }


   // Tests for getPort static method
   @Test
   @DisplayName("Should parse valid port from command line arguments")
   public void testGetPortWithValidPort() throws Exception {
       String[] args = {"8080"};
       int result = invokeGetPort(args);
       assertEquals(8080, result);
   }


   @Test
   @DisplayName("Should return default port when no arguments provided")
   public void testGetPortWithNoArguments() throws Exception {
       String[] args = {};
       int result = invokeGetPort(args);
       assertEquals(DEFAULT_PORT, result);
   }


   @Test
   @DisplayName("Should return default port for invalid port string")
   public void testGetPortWithInvalidPortString() throws Exception {
       String[] args = {"invalid"};
       int result = invokeGetPort(args);
       assertEquals(DEFAULT_PORT, result);
   }


   @Test
   @DisplayName("Should parse port 3000")
   public void testGetPortWith3000() throws Exception {
       String[] args = {"3000"};
       int result = invokeGetPort(args);
       assertEquals(3000, result);
   }


   @Test
   @DisplayName("Should parse port 9999")
   public void testGetPortWith9999() throws Exception {
       String[] args = {"9999"};
       int result = invokeGetPort(args);
       assertEquals(9999, result);
   }


   @Test
   @DisplayName("Should return default port for non-numeric port")
   public void testGetPortWithNonNumericPort() throws Exception {
       String[] args = {"portABC"};
       int result = invokeGetPort(args);
       assertEquals(DEFAULT_PORT, result);
   }


   // Tests for parseInt private method
   @Test
   @DisplayName("Should parse valid integer string")
   public void testParseIntWithValidNumber() throws Exception {
       int result = invokeParseInt("10", 5);
       assertEquals(10, result);
   }


   @Test
   @DisplayName("Should return default value for invalid integer string")
   public void testParseIntWithInvalidString() throws Exception {
       int result = invokeParseInt("notanumber", 10);
       assertEquals(10, result);
   }


   @Test
   @DisplayName("Should return default value for empty string")
   public void testParseIntWithEmptyString() throws Exception {
       int result = invokeParseInt("", 15);
       assertEquals(15, result);
   }


   @Test
   @DisplayName("Should parse zero correctly")
   public void testParseIntWithZero() throws Exception {
       int result = invokeParseInt("0", 5);
       assertEquals(0, result);
   }


   @Test
   @DisplayName("Should parse negative numbers correctly")
   public void testParseIntWithNegativeNumber() throws Exception {
       int result = invokeParseInt("-5", 10);
       assertEquals(-5, result);
   }


   // Tests for Server constructor
   @Test
   @DisplayName("Server constructor should set port")
   public void testServerConstructorSetsPort() throws Exception {
       int portValue = getServerPort(server);
       assertEquals(DEFAULT_PORT, portValue);
   }


   @Test
   @DisplayName("Server constructor should initialize with custom port")
   public void testServerConstructorWithCustomPort() throws Exception {
       Server customServer = new Server(8080);
       int portValue = getServerPort(customServer);
       assertEquals(8080, portValue);
   }


   // Tests for launchRobot method
   @Test
   @DisplayName("Should launch robot with name only")
   public void testLaunchRobotWithNameOnly() throws Exception {
       String[] parts = {"launch", "RoboBot"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("RoboBot"));
       assertTrue(response.contains("Launched robot"));
   }


   @Test
   @DisplayName("Should launch robot with name and kind")
   public void testLaunchRobotWithNameAndKind() throws Exception {
       String[] parts = {"launch", "TestBot", "heavy"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("TestBot"));
       assertTrue(response.contains("heavy"));
   }


   @Test
   @DisplayName("Should launch robot with custom shields and shots")
   public void testLaunchRobotWithCustomStats() throws Exception {
       String[] parts = {"launch", "PowerBot", "normal", "10", "20"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("PowerBot"));
       assertTrue(response.contains("10"));
       assertTrue(response.contains("20"));
   }


   @Test
   @DisplayName("Should return error message when name is missing")
   public void testLaunchRobotWithoutName() throws Exception {
       String[] parts = {"launch"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("Usage"));
   }


   @Test
   @DisplayName("Should use default values for missing shields and shots")
   public void testLaunchRobotWithDefaultStats() throws Exception {
       String[] parts = {"launch", "DefaultBot"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains(String.valueOf(DEFAULT_SHIELDS)));
       assertTrue(response.contains(String.valueOf(DEFAULT_SHOTS)));
   }


   @Test
   @DisplayName("Should use default shields when invalid value provided")
   public void testLaunchRobotWithInvalidShields() throws Exception {
       String[] parts = {"launch", "InvalidBot", "normal", "invalid", "5"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("InvalidBot"));
       assertTrue(response.contains(String.valueOf(DEFAULT_SHIELDS)));
   }


   @Test
   @DisplayName("Should use default shots when invalid value provided")
   public void testLaunchRobotWithInvalidShots() throws Exception {
       String[] parts = {"launch", "InvalidBot2", "normal", "5", "invalid"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("InvalidBot2"));
       assertTrue(response.contains(String.valueOf(DEFAULT_SHOTS)));
   }


   @Test
   @DisplayName("Should use default kind as 'normal' when not specified")
   public void testLaunchRobotDefaultKind() throws Exception {
       String[] parts = {"launch", "NormalBot"};
       String response = invokeLaunchRobot(parts, mockSocket);
       assertTrue(response.contains("normal"));
   }


   /**
    * Helper method to invoke the private getPort static method.
    */
   private int invokeGetPort(String[] args) throws Exception {
       Method method = Server.class.getDeclaredMethod("getPort", String[].class);
       method.setAccessible(true);
       return (int) method.invoke(null, (Object) args);
   }


   /**
    * Helper method to invoke the private parseInt method.
    */
   private int invokeParseInt(String value, int defaultValue) throws Exception {
       Method method = LaunchCommand.class.getDeclaredMethod("parseInt", String.class, int.class);
       method.setAccessible(true);
       return (int) method.invoke(new LaunchCommand(server), value, defaultValue);
   }


   /**
    * Helper method to invoke the private launchRobot method.
    */
   private String invokeLaunchRobot(String[] parts, Socket clientSocket) throws Exception {
       za.co.wethinkcode.robots.protocol.CommandRequest req = new za.co.wethinkcode.robots.protocol.CommandRequest(
               parts[0], java.util.Arrays.asList(parts).subList(1, parts.length)
       );
       return new LaunchCommand(server).execute(req, clientSocket);
   }


   /**
    * Helper method to get the private port field using reflection.
    */
   private int getServerPort(Server serverInstance) throws Exception {
       java.lang.reflect.Field portField = Server.class.getDeclaredField("port");
       portField.setAccessible(true);
       return (int) portField.get(serverInstance);
   }
}
