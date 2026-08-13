package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import za.co.wethinkcode.robots.protocol.CommandRequest;

import java.net.Socket;
import java.util.List;

// Brief: Command to turn selected robots by a specified direction/angle.
public class TurnCommand implements Command {
    private final Server server;

    public TurnCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute(CommandRequest request, Socket clientSocket) {
        List<Robot> selectedRobots = server.getSelectedRobots(request, clientSocket);
        if (selectedRobots.isEmpty()) return "Error: You must launch a robot first.";
        
        List<String> args = request.getArguments();
        String turnDir = "";
        if (args != null && !args.isEmpty()) {
            turnDir = args.get(args.size() - 1).toLowerCase();
        } else if (request.getRobot() != null && !request.getRobot().isBlank()) {
            String[] parts = request.getRobot().trim().split("\\s+");
            turnDir = parts[parts.length - 1].toLowerCase();
        }

        if (turnDir.isEmpty()) return "Error: Missing argument 'left' or 'right'.";
        if (!turnDir.equals("left") && !turnDir.equals("right")) return "Error: Invalid turn direction.";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedRobots.size(); i++) {
            Robot r = selectedRobots.get(i);
            
            String[] dirs = {"NORTH", "EAST", "SOUTH", "WEST"};
            int idx = java.util.Arrays.asList(dirs).indexOf(r.getDirection().toUpperCase());
            if (idx == -1) idx = 0;
            r.setDirection(turnDir.equals("right") ? dirs[(idx + 1) % 4] : dirs[(idx + 3) % 4]);
            
            sb.append("[").append(r.getName()).append("] Turned ").append(turnDir.toUpperCase())
              .append(". Now facing ").append(r.getDirection()).append(".");
            if (i < selectedRobots.size() - 1) sb.append("\n--------------------\n");
        }
        return sb.toString();
    }
}