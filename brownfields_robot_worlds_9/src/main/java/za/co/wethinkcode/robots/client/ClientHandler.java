//
package za.co.wethinkcode.robots.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<String> myRobots = new HashSet<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                PrintStream out = new PrintStream(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println(JsonHandler.formatResponse(in.readLine()));
            runCommandLoop(out, in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runCommandLoop(PrintStream out, BufferedReader in) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine();
            InputParser userInput = new InputParser(input);

            if (!processUserInput(userInput, out, in)) {
                System.out.println("Disconnecting...");
                break;
            }
        }
    }

    private boolean processUserInput(InputParser userInput, PrintStream out, BufferedReader in) throws IOException {
        if (!userInput.isValid()) {
            System.out.println(userInput.getError());
            System.out.println("Please enter valid command");
            return true;
        }
        return handleCommandInput(userInput, out, in);
    }

    private boolean handleCommandInput(InputParser userInput, PrintStream out, BufferedReader in) throws IOException {
        if (!hasRobotAccess(userInput)) {
            return true;
        }

        String jsonRequest = JsonHandler.createRequest(userInput.getRobotName(), userInput);
        out.println(jsonRequest);

        String line = in.readLine();
        String response = JsonHandler.formatResponse(line);
        System.out.println(response);

        updateRobotState(userInput, response);
        checkForRemovalNotification(line);

        return !"quit".equals(userInput.getCommand());
    }

    private boolean hasRobotAccess(InputParser userInput) {
        if (isCommandAllowed(userInput)) {
            return true;
        }
        System.out.println("You don't have a robot named '" + userInput.getRobotName()
                + "'. Robots you've launched: " + myRobots);
        return false;
    }

    private boolean isCommandAllowed(InputParser userInput) {
        String command = userInput.getCommand();
        return "launch".equals(command) || "quit".equals(command) || myRobots.contains(userInput.getRobotName());
    }

    private void updateRobotState(InputParser userInput, String response) {
        if ("launch".equals(userInput.getCommand()) && !response.toLowerCase().contains("error")) {
            myRobots.add(userInput.getRobotName());
        }
    }

    private void checkForRemovalNotification(String line) {
        String msg = JsonHandler.extractMessage(line);
        if (msg == null) {
            return;
        }
        String lowerMsg = msg.toLowerCase();
        if (lowerMsg.contains("removed from the server") || lowerMsg.contains("removed from the world")) {
            promptReconnect(msg);
        }
    }

    private void promptReconnect(String msg) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(null,
                    msg + "\n\nReconnect?",
                    "Disconnected",
                    JOptionPane.OK_CANCEL_OPTION);
            if (choice == JOptionPane.OK_OPTION) {
                System.out.println("Please restart the client.");
            } else {
                System.out.println("Exiting...");
                System.exit(0);
            }
        });
    }
}