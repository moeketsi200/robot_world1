package za.co.wethinkcode.robots.server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.world.World;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final World world;
    private String robotName;
    private PrintWriter out;

    public ClientHandler(Socket socket, World world, String robotName) {
        this.socket = socket;
        this.world = world;
        this.robotName = robotName;
    }

    // Backwards-compatible constructor used by Server when robot name is not yet known
    public ClientHandler(Socket socket, World world) {
        this(socket, world, null);
    }

    @Override
    public void run() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);

            out.println(okMessage("Welcome to Robot World!"));
            processClientSession(in);
        } catch (Exception e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            cleanupSession(in);
        }
    }

    private void processClientSession(BufferedReader in) throws Exception {
        String input;
        while ((input = in.readLine()) != null) {
            System.out.println("Robot says: " + input);
            if (!handleCommand(input, out)) {
                break; // Client quit or an error occurred that should terminate the session.
            }
        }
    }

    private void cleanupSession(BufferedReader in) {
        closeStreams(in);
        cleanupRobotFromWorld();
    }

    private void closeStreams(BufferedReader in) {
        try { if (out != null) out.close(); } catch (Exception ignore) {}
        try { if (in != null) in.close(); } catch (Exception ignore) {}
    }

    private void cleanupRobotFromWorld() {
        try {
            if (robotName != null && world.getRobot(robotName) != null) {
                world.unregisterClientHandler(robotName);
                world.removeRobot(robotName, false);
                System.out.println("Removed robot " + robotName + " from world");
            }
        } catch (Exception ex) {
            System.out.println("Error during disconnect cleanup: " + ex.getMessage());
        }
    }

    /**
     * Handles a single command string from the client.
     * @param input The raw JSON string from the client.
     * @param out The PrintWriter to send the response to.
     * @return false if the client quit, true otherwise.
     */
    private boolean handleCommand(String input, PrintWriter out) {
        try {
            Request request = new Request(input);
            if (!validateRequest(request, out)) {
                return true;
            }
            Command command = request.buildCommand();
            String result = command.execute(world);

            // IMPORTANT: update session state (which registers this handler with the
            // world for launch) BEFORE sending the response to the client. Otherwise
            // there's a race where the client can receive "OK" and immediately act on
            // it (e.g. trigger a purge) before this handler is registered, causing it
            // to be skipped and its readPushedNotification() to hang/timeout.
            updateSessionState(request, command, result);
            sendResponse(command, result, out);

            return !request.isQuit();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument/command: " + e.getMessage());
            out.println(error(e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error parsing command: " + e.getMessage());
            out.println(error("Invalid command format"));
        }
        return true;
    }

    /**
     * Sends the appropriate OK or ERROR response to the client.
     * @param command The command that was executed.
     * @param result  The result string from the command's execution.
     * @param out     The PrintWriter to send the response to.
     */
    private void sendResponse(Command command, String result, PrintWriter out) {
        if (result.startsWith("ERROR")) {
            out.println(error(result.replaceFirst("^ERROR:\\s*", "")));
        } else {
            JsonObject data = command.buildResponse(world, result);
            // Attach state for commands that involve a robot
            String rName = command.getRobotName();
            Robot robot = (rName != null) ? world.getRobot(rName) : null;
            JsonObject state = (robot != null) ? buildState(robot) : null;
            out.println(ok(data, state));
        }
    }

    /**
     * Updates the session state based on the command that was just executed.
     * Registers this handler with the world on a successful launch so the
     * server can notify it later (e.g. on purge). Must run before the
     * response is sent to the client — see the comment in handleCommand.
     * @param request The original request.
     * @param command The command that was executed.
     * @param result The result string from the command's execution.
     */
    private void updateSessionState(Request request, Command command, String result) {
        if (request.isLaunch() && !result.startsWith("ERROR")) {
            this.robotName = command.getRobotName();
            // register this handler so the server can notify it on purge
            world.registerClientHandler(this.robotName, this);
        }
    }

    /**
     * Performs pre-execution validation checks on the request.
     * @param request The request to validate.
     * @param out The PrintWriter to send error messages to.
     * @return true if the request is valid to execute, false otherwise.
     */
    private boolean validateRequest(Request request, PrintWriter out) {
        if (!Command.isSupported(request.getCommandName())) {
            out.println(error("Unsupported command: " + request.getCommandName()));
            return false;
        }

        if (request.requiresRobot() && this.robotName == null) {
            out.println(error("Launch robot first"));
            return false;
        }

        if (request.isLaunch() && this.robotName != null) {
            out.println(error("Too many of you in this world"));
            return false;
        }

        return true;
    }

    public void disconnectClient() {
        try {
            System.out.println("You have been disconnected from the world...");
            if (socket != null && !socket.isClosed()) {
                try { socket.shutdownOutput(); } catch (Exception ignore) {}
                try { socket.shutdownInput(); } catch (Exception ignore) {}
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Error disconnecting client: " + e.getMessage());
        }
    }

    /*
   This method is used to notify the client that it has been removed from the world by the server.
   It sends a message to the client indicating that it has been removed from the world.

 */
    public void notifyRemovedByServer() {
        try {
            if (out != null) {
                out.println(error("You have been removed from the server PLEASE try to reconnect"));
                out.flush();
            }
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        } catch (Exception e) {
            System.out.println("Error notifying client of removal: " + e.getMessage());
        } finally {
            try {
                // Ensure we unregister and close connection
                if (robotName != null) world.unregisterClientHandler(robotName);
            } catch (Exception ignore) {}
            disconnectClient();
        }
    }

    private static String okMessage(String message) {
        JsonObject data = new JsonObject();
        data.addProperty("message", message);
        return ok(data);
    }

    private static String ok(JsonObject data, JsonObject state) {
        JsonObject r = new JsonObject();
        r.addProperty("result", "OK");
        r.add("data", data);
        if (state != null) {
            r.add("state", state);
        }
        return r.toString();
    }

    private static String ok(JsonObject data) {
        return ok(data, null);
    }

    private static JsonObject buildState(Robot robot) {
        JsonArray position = new JsonArray();
        position.add(robot.getPosition().getX());
        position.add(robot.getPosition().getY());

        JsonObject state = new JsonObject();
        state.add("position", position);
        state.addProperty("direction", robot.getDirection().toString());
        state.addProperty("shields", robot.getShields());
        state.addProperty("shots", robot.getShots());
        state.addProperty("status", robot.getStatus().toString());
        return state;
    }

    private static String error(String message) {
        JsonObject data = new JsonObject();
        data.addProperty("message", message);
        JsonObject r = new JsonObject();
        r.addProperty("result", "ERROR");
        r.add("data", data);
        return r.toString();
    }
}