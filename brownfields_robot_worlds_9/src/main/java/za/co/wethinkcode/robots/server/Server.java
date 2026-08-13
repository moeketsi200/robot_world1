package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.config.ConfigLoader;
import za.co.wethinkcode.robots.server.config.WorldConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import za.co.wethinkcode.robots.server.commands.ServerCommands;
import za.co.wethinkcode.robots.server.world.World;
import java.util.Scanner;

public class Server {

    public static void main(String[] args){

        // Load config and override with CLI args (-p, -s, -o) if provided
        WorldConfig config = ConfigLoader.loadConfig("config.json", args);
        World world = new World(config);

        String  ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e){
            ipAddress = "127.0.0.1 (Localhost only)";
        }

        // Add shutdown hook for clean exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nServer shutting down...");
        }));

        try (ServerSocket serverSocket = new ServerSocket(config.getPort())){
            System.out.println("+----------------------------------------------------------+");
            System.out.println("Robot World Server is running.");
            System.out.println("CONNECT TO THIS SERVER USING");
            System.out.println(" IP Address : " + ipAddress);
            System.out.println(" Port       : " + config.getPort());
            System.out.println("World size  : " + config.getWorldSize() + "x" + config.getWorldSize());;
            System.out.println("Press Ctrl+C to stop the server\n");
            System.out.println("+----------------------------------------------------------+");
            // Only start the server-side console commands handler if a console is available
            if (System.console() != null) {
                new Thread(new ServerCommands(world)).start();
            }

            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("\nNew robot connection request received.");

                // Spawn a new independent thread per client session (Multi-threading)
                ClientHandler handler = new ClientHandler(socket, world);
                new Thread(handler).start();
            }

        } catch (IOException e){
            System.out.println("Could not start server: " + e.getMessage());
        }
    }
}