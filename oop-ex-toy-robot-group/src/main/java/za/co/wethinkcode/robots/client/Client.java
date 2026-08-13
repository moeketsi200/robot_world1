package za.co.wethinkcode.robots.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import za.co.wethinkcode.robots.protocol.JsonProtocol;

// Brief: CLI client that connects to the robot server, sends console input, and prints responses.
public class Client {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;
    private static volatile boolean shutdown = false;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? getPort(args[1]) : DEFAULT_PORT;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))
        ) {


            System.out .println(JsonProtocol.messageFromResponseLine(serverIn.readLine()));
            System.out.println("Enter messages to send to the server:");

            java.util.concurrent.atomic.AtomicReference<String> lastCommand = new java.util.concurrent.atomic.AtomicReference<>("none");

            // Keep listening for server responses while the user is typing commands.
            Thread listener = new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        // The server uses END as an internal marker after multi-line messages.
                        if (!"END".equals(line)) {
                            System.out.println(JsonProtocol.messageFromResponseLine(line));
                            System.out.println("What is next? (Last command: " + lastCommand.get() + ")");
                        }
                    }
                } catch (IOException e) {
                    if (!shutdown) {
                        System.out.println("Connection to server was lost. Please type 'quit' to quit.");
                    }
                }
            });
            listener.setDaemon(true);
            listener.start();

            String input;

            while ((input = keyboard.readLine()) != null) {
                if (!input.trim().isEmpty()) {
                    lastCommand.set(input.trim());
                }
                serverOut.println(JsonProtocol.requestFromConsoleInput(input));

                if ("quit".equalsIgnoreCase(input.trim())) {
                    shutdown = true;
                    break;
                }
            }

        } catch (ConnectException e) {
            System.err.println("Connection refused: Could not connect to " + host + ":" + port + ".");
            System.err.println("Please check if the server is running in a separate terminal and the IP address is correct.");
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static int getPort(String port) {
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port '" + port + "'. Using " + DEFAULT_PORT + ".");
            return DEFAULT_PORT;
        }
    }
}
