package za.co.wethinkcode.robots.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.robots.protocol.CommandRequest;
import za.co.wethinkcode.robots.protocol.JsonProtocol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientTest {

    private InputStream originalIn;
    private PrintStream originalOut;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    public void setUp() {
        // Save the real System.in and System.out
        originalIn = System.in;
        originalOut = System.out;
        
        // Create a new stream to capture what the Client prints to the console
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    public void tearDown() {
        // Restore the real System.in and System.out after the test finishes
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testClientConnectsAndSendsMessage() throws InterruptedException, IOException {
        int testPort = 5051;

        // 1. Set up a dummy server to pretend to be the real Server
        Thread dummyServer = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(testPort)) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Send the JSON welcome message the Client expects
                out.println(JsonProtocol.ok("Dummy Server Connected"));

                // Read the message the Client sends us
                CommandRequest request = JsonProtocol.requestFromJson(in.readLine());
                assertEquals("launch", request.getCommand());
                assertEquals("TestBot", request.getArguments().get(0));
            } catch (IOException e) {
                // Ignore exceptions in dummy server
            }
        });
        dummyServer.start();

        // Give the dummy server a split second to start
        Thread.sleep(200);

        // 2. Simulate the user typing "launch TestBot" and then "quit" into the console
        String simulatedUserInput = "launch TestBot\nquit\n";
        ByteArrayInputStream testIn = new ByteArrayInputStream(simulatedUserInput.getBytes());
        System.setIn(testIn);

        // 3. Run the Client, pointing it at our dummy server port
        Client.main(new String[]{"localhost", String.valueOf(testPort)});

        // Give the Client's background listener thread a moment to finish printing
        Thread.sleep(200);

        // 4. Verify that the Client printed the correct expected output
        String output = testOut.toString();
        assertTrue(output.contains("Dummy Server Connected"), "Client should print the server's welcome message");
        assertTrue(output.contains("Enter messages to send to the server:"), "Client should prompt user for input");
    }
}
