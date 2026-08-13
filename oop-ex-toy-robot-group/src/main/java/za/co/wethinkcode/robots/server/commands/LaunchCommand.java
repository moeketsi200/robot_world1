package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;
import java.util.Set;

// Brief: Command to launch a new robot for the requesting client with provided name/kind.
public class LaunchCommand implements Command {
    private final Server server;
    private static final int DEFAULT_SHIELDS = 5;
    private static final int DEFAULT_SHOTS = 5;
    private static final Set<String> VALID_KINDS = Set.of("normal", "sniper", "bomber", "heavy");

    public LaunchCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<String> arguments = request.getArguments();
        if (arguments == null) {
            arguments = List.of();
        }
        if (arguments.isEmpty() && request.getRobot() != null && !request.getRobot().isBlank()) {
            arguments = List.of(request.getRobot());
        }

        if (arguments.isEmpty()) {
            return "Usage: launch <name> [kind] [shields] [shots]";
        }

        String name = arguments.get(0);
        String kind = arguments.size() > 1 ? arguments.get(1) : "normal";

        if (arguments.size() > 1 && !VALID_KINDS.contains(kind.toLowerCase())) {
            return "Error: Invalid robot kind '" + kind + "'. Valid kinds are: " + VALID_KINDS;
        }

        // If shields not explicitly provided, use the world's configured default.
        int shields = arguments.size() > 2
            ? parseInt(arguments.get(2), DEFAULT_SHIELDS)
            : server.getWorld().getDefaultShieldStrength();

        int shots = arguments.size() > 3 ? parseInt(arguments.get(3), DEFAULT_SHOTS) : DEFAULT_SHOTS;

        Robot robot = new Robot(name, kind, shields, shots);
        if (!server.getWorld().placeRobot(robot)) {
            return "Could not launch robot: there is no open position in the world.";
        }

        server.addClientRobot(clientSocket, robot);
        return "Launched robot: " + robot;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}