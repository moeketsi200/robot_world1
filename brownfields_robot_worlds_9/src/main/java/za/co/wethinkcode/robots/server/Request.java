//package za.co.wethinkcode.robots.server;
//
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import za.co.wethinkcode.robots.server.commands.Command;
//
//public class Request {
//    private final String robotName;
//    private final String commandName;
//    private final JsonObject json;
//
//    public Request(String robotName, String jsonString) {
//        this.robotName = robotName;
//        this.json = JsonParser.parseString(jsonString).getAsJsonObject();
//        this.commandName = this.json.get("command").getAsString().toLowerCase();
//    }
//
//    public String getCommandName() {
//        return this.commandName;
//    }
//
//    public Command buildCommand() {
//        // Add the current robot's name to the request for the factory
//        if (this.robotName != null) {
//            this.json.addProperty("robot", this.robotName);
//        }
//        return Command.create(this.json);
//    }
//
//    public boolean isLaunch() {
//        return "launch".equals(this.commandName);
//    }
//
//    public boolean isQuit() {
//        return "quit".equals(this.commandName);
//    }
//
//    public boolean requiresRobot() {
//        return !isLaunch() && !isQuit();
//    }
//}
package za.co.wethinkcode.robots.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import za.co.wethinkcode.robots.server.commands.Command;

public class Request {
    private final String robotName;
    private final String commandName;
    private final JsonObject json;

    public Request(String jsonString) {
        this.json = JsonParser.parseString(jsonString).getAsJsonObject();
        this.commandName = this.json.get("command").getAsString().toLowerCase();
        // Robot name now comes FROM the client's request, not injected by the session.
        this.robotName = this.json.has("robot") ? this.json.get("robot").getAsString() : null;
    }

    public String getRobotName() {
        return this.robotName;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public Command buildCommand() {
        // No longer needs to inject "robot" - it's already in the JSON the client sent.
        return Command.create(this.json);
    }

    public boolean isLaunch() {
        return "launch".equals(this.commandName);
    }

    public boolean isQuit() {
        return "quit".equals(this.commandName);
    }

    public boolean requiresRobot() {
        return !isLaunch() && !isQuit();
    }
}