package za.co.wethinkcode.robots.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonHandler {
    private static final Gson gson = new Gson();
    // builds a JSON request for the server.
    // format : { "robot": "name", "command": "cmd", "arguments": ["arg1"] }

//    public static String createRequest(String roboName, InputParser parser){
//        String command = parser.getCommand();
//        String[] parts = parser.getParts();
//
//        List<String> arguments = new ArrayList<>();
//        if (parts.length > 1){
//            arguments.addAll(Arrays.asList(parts).subList(1, parts.length));
//        }
//        Map<String, Object> requestMap = Map.of(
//                "robot", roboName,
//                "command", command,
//                "arguments", arguments
//        );
//        return gson.toJson(requestMap);
//    }
        public static String createRequest(String robotName, InputParser parser){
            String command = parser.getCommand();
            List<String> arguments = new ArrayList<>(Arrays.asList(parser.getCommandArgs()));

            Map<String, Object> requestMap = Map.of(
                    "robot", robotName != null ? robotName : "",
                    "command", command,
                    "arguments", arguments
            );
            return gson.toJson(requestMap);
        }

    public static String extractMessage(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject data = json.getAsJsonObject("data");
            if (data != null && data.has("message")) {
                return data.get("message").getAsString();
            }
            return jsonResponse; // fallback — return raw if no message field
        } catch (Exception e) {
            return jsonResponse; // fallback — return raw if parsing fails
        }
    }

    public static String formatResponse(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject data = json.getAsJsonObject("data");
            if (data == null) return jsonResponse;

            // Simple message response
            if (data.has("message")) {
                return data.get("message").getAsString();
            }


            StringBuilder string = new StringBuilder();

            for (String key : data.keySet()) {

                if (data.get(key).isJsonArray()) {
                    if ("position".equals(key)) {
                        string.append("position: ").append(data.get(key)).append("\n");
                    } else {
                        for (JsonElement element : data.get(key).getAsJsonArray()) {
                            string.append(element.getAsString()).append("\n");
                        }
                    }
                } else {
                    string.append(key).append(": ").append(data.get(key).getAsString()).append("\n");
                }
            }
            return string.toString().trim();

        } catch (Exception e) {
            return jsonResponse; // fallback — return raw if parsing fails
        }
    }


}
