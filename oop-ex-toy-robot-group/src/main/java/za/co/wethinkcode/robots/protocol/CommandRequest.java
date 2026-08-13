package za.co.wethinkcode.robots.protocol;

import java.util.ArrayList;
import java.util.List;

// Brief: Parsed command request from a client, including command name and arguments.
public class CommandRequest {
    // JSON clients can send the robot name separately from command arguments.
    private String robot;
    private String command;
    private List<String> arguments = new ArrayList<>();

    public CommandRequest() {
    }

    public CommandRequest(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String[] toCommandParts() {
        String[] parts = new String[arguments.size() + 1];
        parts[0] = command == null ? "" : command;
        for (int i = 0; i < arguments.size(); i++) {
            parts[i + 1] = arguments.get(i);
        }
        return parts;
    }
}
