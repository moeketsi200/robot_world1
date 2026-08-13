package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.commands.*;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.robots.protocol.CommandRequest;
import za.co.wethinkcode.robots.protocol.JsonProtocol;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// Brief: Main server that accepts client connections and dispatches commands to the world.
public class Server {
    private static final int DEFAULT_PORT = 5000;
    // private static final int DEFAULT_SHIELDS = 5;
    // private static final int DEFAULT_SHOTS = 5;
    private static final int DEFAULT_WORLD_SIZE = 200;
    private final List<Socket> activeClients = new CopyOnWriteArrayList<>();
    private final Map<Socket, List<Robot>> robots = new ConcurrentHashMap<>();
    private final Map<String, Command> commands = new HashMap<>();
    private final World world;
    private ServerSocket serverSocketInstance;
    private final int port;

    public Server(int port) {
        this.port = port;
        WorldConfig config = loadConfig("config.json");
        if (config != null) {
            this.world = new World(config);
        } else {
            this.world = new World(DEFAULT_WORLD_SIZE, DEFAULT_WORLD_SIZE);
        }
        initializeCommands();
    }

    private void initializeCommands() {
        commands.put("launch", new LaunchCommand(this));
        commands.put("state", new StateCommand(this));
        commands.put("look", new LookCommand(this));
        commands.put("fire", new FireCommand(this));
        commands.put("repair", new RepairCommand(this));
        commands.put("reload", new ReloadCommand(this));
        commands.put("forward", new ForwardCommand(this));
        commands.put("back", new BackCommand(this));
        commands.put("left", new LeftCommand(this));
        commands.put("right", new RightCommand(this));
        commands.put("turn", new TurnCommand(this));
        commands.put("push", new PushCommand(this));
        commands.put("robots", (req, sock) -> new RobotsCommand(this).executeForClient(req, sock));
    }

    private WorldConfig loadConfig(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File configFile = new File(filename);
            if (configFile.exists()) {
                return mapper.readValue(configFile, WorldConfig.class);
            } else {
                System.err.println("Config file not found: " + filename + ". Using defaults.");
            }
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage() + ". Using defaults.");
        }
        return null;
    }

    public World getWorld() {
        return world;
    }

    public static void main(String[] args) {
        int port = getPort(args);
        Server server = new Server(port);
        new Thread(server::start).start();

        System.out.println("Server command line open. Type 'quit', 'dump', or 'robots'.");
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while ((command = console.readLine()) != null) {
                switch (command.trim().toLowerCase()) {
                    case "quit":
                        new QuitCommand(server).execute();
                        break;
                    case "dump":
                        new DumpCommand(server).execute();
                        break;
                    case "robots":
                        new RobotsCommand(server).execute();
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from console: " + e.getMessage());
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            this.serverSocketInstance = serverSocket;
            System.out.println("Server listening on port " + this.port);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                activeClients.add(clientSocket);
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            if (serverSocketInstance != null && serverSocketInstance.isClosed()) {
                System.out.println("Server socket closed.");
            } else {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("Client connected: " + clientAddress);

        try (
                Socket socket = clientSocket;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println(JsonProtocol.ok("Connected to robot server. Type 'launch <name>' to create your robot, or 'quit' to quit.", world));

            String message;
            while ((message = in.readLine()) != null) {
                CommandRequest request = readRequest(message);
                String command = request.getCommand() == null ? "" : request.getCommand().toLowerCase();

                if (!"robots".equals(command)) {
                    System.out.println("Received from " + clientAddress + ": " + message);
                }

                if ("quit".equals(command)) {
                    sendResponse(out, "Goodbye.");
                    break;
                }

                Command cmd = commands.get(command);
                if (cmd != null) {
                    String responseStr = cmd.execute(request, clientSocket);
                    if ("robots".equals(command)) {
                        out.println(responseStr);
                    } else if ("fire".equals(command) && !responseStr.toLowerCase().startsWith("error")) {
                        broadcastResponse(responseStr);
                    } else {
                        sendResponse(out, responseStr);
                    }
                } else {
                    sendResponse(out, "Server received: " + command);
                }
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                System.err.println("Client error: " + e.getMessage());
            }
        } finally {
            System.out.println("Client disconnected: " + clientAddress);
            activeClients.remove(clientSocket);
            List<Robot> clientRobots = robots.remove(clientSocket);
            if (clientRobots != null) {
                clientRobots.forEach(world::removeRobot);
            }
        }
    }

    // Public helper to select robots for a request (used by command classes)
    public java.util.List<Robot> selectRobotsForRequest(za.co.wethinkcode.robots.protocol.CommandRequest request, Socket clientSocket) {
        java.util.List<Robot> clientRobots = robots.get(clientSocket);
        if (clientRobots == null || clientRobots.isEmpty()) {
            return java.util.List.of();
        }

        String robotName = null;
        if (request.getRobot() != null && !request.getRobot().isBlank()) {
            robotName = request.getRobot();
        }
        java.util.List<String> arguments = request.getArguments();
        if (robotName == null && arguments != null && !arguments.isEmpty() && !arguments.get(0).isBlank()) {
            robotName = arguments.get(0);
        }
        if (robotName == null) {
            return clientRobots;
        }

        for (Robot robot : clientRobots) {
            if (robot.getName().equalsIgnoreCase(robotName)) {
                return java.util.List.of(robot);
            }
        }
        return java.util.List.of();
    }

    // Rotate selected robots left/right
    public String rotateSelectedRobots(za.co.wethinkcode.robots.protocol.CommandRequest request, Socket clientSocket, String turn) {
        java.util.List<Robot> selected = selectRobotsForRequest(request, clientSocket);
        if (selected.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        String[] dirs = new String[] {"NORTH", "EAST", "SOUTH", "WEST"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selected.size(); i++) {
            Robot r = selected.get(i);
            int idx = java.util.Arrays.asList(dirs).indexOf(r.getDirection());
            if (idx == -1) idx = 0;
            if ("left".equalsIgnoreCase(turn)) {
                idx = (idx + 3) % 4; // -1 mod 4
            } else {
                idx = (idx + 1) % 4;
            }
            r.setDirection(dirs[idx]);
            sb.append("[").append(r.getName()).append("] Facing " ).append(r.getDirection());
            if (i < selected.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    // Move selected robots forward/back by a distance (positive forward, negative back)
    public String moveSelectedRobots(za.co.wethinkcode.robots.protocol.CommandRequest request, Socket clientSocket, int distance) {
        java.util.List<Robot> selected = selectRobotsForRequest(request, clientSocket);
        if (selected.isEmpty()) {
            return "Error: You must launch a robot first.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selected.size(); i++) {
            Robot r = selected.get(i);
            int dx = 0, dy = 0;
            switch (r.getDirection()) {
                case "NORTH": dy = -1; break;
                case "SOUTH": dy = 1; break;
                case "EAST": dx = 1; break;
                case "WEST": dx = -1; break;
                default: dy = -1; break;
            }
            int steps = Math.abs(distance);
            int stepSign = distance >= 0 ? 1 : -1;
            boolean movedAll = true;
            for (int s = 0; s < steps; s++) {
                int nx = r.getX() + dx * stepSign;
                int ny = r.getY() + dy * stepSign;
                if (world.isPositionOpen(nx, ny)) {
                    r.setPosition(nx, ny);
                } else {
                    movedAll = false;
                    break;
                }
            }
            if (movedAll) {
                sb.append("[").append(r.getName()).append("] moved to [").append(r.getX()).append(", ").append(r.getY()).append("]");
            } else {
                sb.append("[").append(r.getName()).append("] Movement blocked at [").append(r.getX()).append(", ").append(r.getY()).append("]");
            }
            if (i < selected.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    static {
        new Recorder().logRun();
    }

    public void shutdown() {
        System.out.println("Shutting down the server...");
        try {
            for (Socket socket : activeClients) {
                if (!socket.isClosed()) {
                    try {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        // Send a clear shutdown banner to every connected client.
                        out.println("=========================================");
                        out.println("    SERVER IS SHUTTING DOWN! Goodbye!   ");
                        out.println("=========================================");
                        out.println("END");
                        // Give the OS network buffer a split second to actually send the message!
                        Thread.sleep(100);
                    } catch (Exception e) {
                        // Ignore if we can't send the message, the client might already be disconnected
                    }
                    socket.close();
                }
            }
            activeClients.clear();


            if (serverSocketInstance != null && !serverSocketInstance.isClosed()) {
                serverSocketInstance.close();
            }
        } catch (IOException e) {
            System.out.println("Error while shutting down: " + e.getMessage());
        }

        System.out.println("Server offline.");
        System.exit(0);
    }

    public void dumpWorld() {
        System.out.println("=====================================");
        System.out.println("          WORLD STATE DUMP           ");
        System.out.println("=====================================");
        System.out.println("World Size: " + world.getWidth() + "x" + world.getHeight());
        System.out.println("Obstacles:");
        for (WorldObject object : world.getObjects()) {
            System.out.println("- " + object.getType() + " at [" + object.getX() + ", " + object.getY() + "]");
        }
        for (WorldObject mine : world.getMines()) {
            System.out.println("- Mine at [" + mine.getX() + ", " + mine.getY() + "] (Owner: " + mine.getOwner() + ")");
        }
        System.out.println("Active Clients connected: " + activeClients.size());
        System.out.println("Robots launched: " + world.getRobots().size());
        for (Robot robot : world.getRobots()) {
            System.out.println("- Robot: " + robot.getName() + " at [" + robot.getX() + ", " + robot.getY() + "]");
        }
        System.out.println("=====================================");
    }

    public void listRobots() {
        System.out.println("=====================================");
        System.out.println("        ACTIVE CONNECTIONS           ");
        System.out.println("=====================================");
        if (world.getRobots().isEmpty()) {
            System.out.println("No robots are currently launched.");
        } else {
            for (Robot robot : world.getRobots()) {
                System.out.println("- Robot: " + robot.getName() + " facing " + robot.getDirection() + " [" + robot.getKind() + "] at [" + robot.getX() + ", " + robot.getY() + "]");
            }
        }
        System.out.println("--------------------------");
    }

    public void addClientRobot(Socket clientSocket, Robot robot) {
        robots.computeIfAbsent(clientSocket, socket -> new CopyOnWriteArrayList<>()).add(robot);
    }

    public void removeRobotFromBoard(Robot robot) {
        world.removeRobot(robot);
        for (List<Robot> clientRobots : robots.values()) {
            clientRobots.remove(robot);
        }
    }

    public List<Robot> getSelectedRobots(CommandRequest request, Socket clientSocket) {
        List<Robot> clientRobots = robots.get(clientSocket);
        if (clientRobots == null || clientRobots.isEmpty()) {
            return List.of();
        }

        // 1. If a robot name is explicitly provided in the request field, use it.
        if (request.getRobot() != null && !request.getRobot().isBlank()) {
            for (Robot robot : clientRobots) {
                if (robot.getName().equalsIgnoreCase(request.getRobot())) {
                    return List.of(robot);
                }
            }
            return List.of(); // Explicitly named robot not found for this client.
        }

        // 2. Otherwise, check if the first argument is a robot name.
        List<String> arguments = request.getArguments();
        if (arguments != null && !arguments.isEmpty() && !arguments.get(0).isBlank()) {
            String potentialName = arguments.get(0);
            for (Robot robot : clientRobots) {
                if (robot.getName().equalsIgnoreCase(potentialName)) {
                    return List.of(robot);
                }
            }
            // Not a robot name, so assume the argument is a command parameter (like steps)
            // and return all robots owned by this client.
        }

        return clientRobots;
    }


    private CommandRequest readRequest(String message) throws JsonProcessingException {
        try {
            return JsonProtocol.requestFromJson(message);
        } catch (JsonProcessingException e) {
            String[] parts = message.trim().split("\\s+");
            if (parts.length == 0 || parts[0].isBlank()) {
                return new CommandRequest("", List.of());
            }
            return new CommandRequest(parts[0], List.of(parts).subList(1, parts.length));
        }
    }

    private void sendResponse(PrintWriter out, String response) throws JsonProcessingException {
        if (response.toLowerCase().startsWith("error")) {
            out.println(JsonProtocol.error(response));
        } else {
            out.println(JsonProtocol.ok(response, world));
        }
    }

    public void broadcastResponse(String response) {
        for (Socket socket : activeClients) {
            if (!socket.isClosed()) {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    sendResponse(out, response);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }


    private static int getPort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port '" + args[0] + "'. Using " + DEFAULT_PORT + ".");
            }
        }
        return DEFAULT_PORT;
    }
}
// Ola