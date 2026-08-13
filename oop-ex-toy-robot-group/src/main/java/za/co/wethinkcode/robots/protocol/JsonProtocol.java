package za.co.wethinkcode.robots.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

import za.co.wethinkcode.robots.server.domain.World;
import za.co.wethinkcode.robots.server.domain.WorldObject;

// Brief: Utility helpers to encode/decode protocol messages to and from JSON.
public final class JsonProtocol {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonProtocol() {
    }

    public static String requestFromConsoleInput(String input) throws JsonProcessingException {
        List<String> parts = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1)); // Add the quoted part (without the quotes)
            } else {
                parts.add(matcher.group(2)); // Add the unquoted word
            }
        }

        if (parts.isEmpty() || parts.get(0).isBlank()) {
            return toJson(new CommandRequest("", Collections.emptyList()));
        }
        
        List<String> arguments = parts.subList(1, parts.size());
        return toJson(new CommandRequest(parts.get(0).toLowerCase(), arguments));
    }

    public static CommandRequest requestFromJson(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, CommandRequest.class);
    }

    public static String ok(String message) throws JsonProcessingException {
        return toJson(new ServerResponse("OK", message));
    }

    public static String ok(String message, World world) throws JsonProcessingException {
        ServerResponse response = new ServerResponse("OK", message);
        List<Map<String, Object>> objectsList = new ArrayList<>();
        if (world != null) {
            for (WorldObject o : world.getObjects()) {
                objectsList.add(Map.of("x", o.getX(), "y", o.getY(), "type", o.getType()));
            }
            if (world.getMines() != null) {
                for (WorldObject m : world.getMines()) {
                    objectsList.add(Map.of("x", m.getX(), "y", m.getY(), "type", m.getType()));
                }
            }
        }
        response.getData().put("objects", objectsList);
        return toJson(response);
    }

    public static String error(String message) throws JsonProcessingException {
        return toJson(new ServerResponse("ERROR", message));
    }

    public static ServerResponse responseFromJson(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, ServerResponse.class);
    }

    public static String messageFromResponseLine(String line) {
        try {
            return responseFromJson(line).getMessage();
        } catch (JsonProcessingException e) {
            return line;
        }
    }

    private static String toJson(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }
}
