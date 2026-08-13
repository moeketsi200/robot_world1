package za.co.wethinkcode.robots.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonSyntaxException;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.CommandParser;

class CommandParserTest {

    @Test
    void parsesValidJsonWithoutCrashing() {
        // A standard JSON payload
        String validJson = "{\"command\": \"launch\", \"arguments\": [\"Scout\", \"5\", \"5\"]}";

        Command result = CommandParser.parsejson(validJson);

        assertNull(result);
    }

    @Test
    void throwsExceptionOnInvalidJson() {
        String badJson = "{ I am not valid JSON }";

        // GSON should throw an error here
        assertThrows(JsonSyntaxException.class, () -> {
            CommandParser.parsejson(badJson);
        });
    }
}