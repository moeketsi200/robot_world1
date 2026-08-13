package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.client.ClientHandler;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String ipAddress;
        int portNumber = 0;
        Scanner scanner = new Scanner(System.in);

        // Check if arguments were passed. If not, ask the user interactively!
        if (args.length == 2) {
            ipAddress = args[0];
            try {
                portNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number argument provided. Falling back to input prompt.");
                portNumber = 0;
            }
        } else {
            // Interactive Prompt Input Mode
            System.out.println("=== Robot World Client Connection Tool ===");
            System.out.print("Enter Server IP Address (e.g., 20.20.20.100): ");
            ipAddress = scanner.nextLine().trim();

            while (portNumber == 0) {
                System.out.print("Enter Port Number (e.g., 5000): ");
                String portInput = scanner.nextLine().trim();
                try {
                    portNumber = Integer.parseInt(portInput);
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid numeric port number.");
                }
            }
        }

        System.out.println("\nConnecting to server target...");
        System.out.println("Target IP   : " + ipAddress);
        System.out.println("Target Port : " + portNumber);

        try (Socket socket = new Socket(ipAddress, portNumber)){
            ClientHandler client = new ClientHandler(socket);
            client.run();

        } catch (IOException e) {
            System.out.println("Fatal: Could not establish a network connection to the server.");
            System.out.println("Please verify the server IP/Port and check your network/firewall configurations.");
        }
    }
}