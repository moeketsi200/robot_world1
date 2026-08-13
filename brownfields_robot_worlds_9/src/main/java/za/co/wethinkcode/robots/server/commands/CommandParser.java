package za.co.wethinkcode.robots.server.commands;

import com.google.gson.*;

public class CommandParser {

    public static Command parsejson(String jsonString) {
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        String command = json.get("command").getAsString();
        return null;
    }
}
