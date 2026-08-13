package za.co.wethinkcode.robots.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonHandlerTest {

    // createRequest() tests
    @Test
    void createRequest_launchCommand_buildsCorrectJson() {
        InputParser parser = new InputParser("launch Scout Hal");
        String result = JsonHandler.createRequest("Hal", parser);

        assertTrue(result.contains("\"robot\":\"Hal\""));
        assertTrue(result.contains("\"command\":\"launch\""));
        assertTrue(result.contains("Scout"));
        assertTrue(result.contains("Hal"));
    }

    @Test
    void createRequest_forwardCommand_includesStepsInArguments() {
        InputParser parser = new InputParser("forward Hal 5");
        String result = JsonHandler.createRequest("Hal", parser);

        assertTrue(result.contains("\"command\":\"forward\""));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("\"robot\":\"Hal\""));
    }

    @Test
    void createRequest_lookCommand_hasEmptyArguments() {
        InputParser parser = new InputParser("look Hal");
        String result = JsonHandler.createRequest("Hal", parser);

        assertTrue(result.contains("\"command\":\"look\""));
        assertTrue(result.contains("\"arguments\":[]"));
    }

    @Test
    void createRequest_turnLeftCommand_includesDirectionInArguments() {
        InputParser parser = new InputParser("turn Hal left");
        String result = JsonHandler.createRequest("Hal", parser);

        assertTrue(result.contains("\"command\":\"turn\""));
        assertTrue(result.contains("left"));
    }

    @Test
    void createRequest_stateCommand_hasEmptyArguments() {
        InputParser parser = new InputParser("state Hal");
        String result = JsonHandler.createRequest("Hal", parser);

        assertTrue(result.contains("\"command\":\"state\""));
        assertTrue(result.contains("\"arguments\":[]"));
    }

    @Test
    void createRequest_usesRobotNameProvided() {
        InputParser parser = new InputParser("look Hal");
        String result = JsonHandler.createRequest("MyRobot", parser);

        assertTrue(result.contains("\"robot\":\"MyRobot\""));
    }

    @Test
    void createRequest_backCommand_includesStepsInArguments() {
        InputParser parser = new InputParser("back Hal 3");
        String result = JsonHandler.createRequest("Hal", parser);

        assertTrue(result.contains("\"command\":\"back\""));
        assertTrue(result.contains("3"));
    }

    // extractMessage() tests
    @Test
    void extractMessage_simpleMessage_returnsMessageText() {
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"Welcome to Robot World!\"}}";
        String result = JsonHandler.extractMessage(json);

        assertEquals("Welcome to Robot World!", result);
    }

    @Test
    void extractMessage_errorMessage_returnsErrorText() {
        String json = "{\"result\":\"ERROR\",\"data\":{\"message\":\"Robot not found\"}}";
        String result = JsonHandler.extractMessage(json);

        assertEquals("Robot not found", result);
    }

    @Test
    void extractMessage_noMessageField_returnsRawJson() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":\"5,3\"}}";
        String result = JsonHandler.extractMessage(json);

        assertEquals(json, result);
    }

    @Test
    void extractMessage_invalidJson_returnsRawInput() {
        String notJson = "this is not json";
        String result = JsonHandler.extractMessage(notJson);

        assertEquals(notJson, result);
    }

    @Test
    void extractMessage_emptyString_returnsEmptyString() {
        String result = JsonHandler.extractMessage("");
        assertEquals("", result);
    }

    @Test
    void extractMessage_nullData_returnsRawJson() {
        String json = "{\"result\":\"OK\"}";
        String result = JsonHandler.extractMessage(json);
        assertEquals(json, result);
    }

    // formatResponse() tests — message responses
    @Test
    void formatResponse_welcomeMessage_returnsMessageOnly() {
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"Welcome to Robot World!\"}}";
        String result = JsonHandler.formatResponse(json);

        assertEquals("Welcome to Robot World!", result);
    }

    @Test
    void formatResponse_launchSuccess_returnsMessageOnly() {
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"Robot launched\",\"position\":\"5,3\",\"visibility\":5}}";
        String result = JsonHandler.formatResponse(json);

        assertEquals("Robot launched", result);
    }

    @Test
    void formatResponse_errorMessage_returnsErrorText() {
        String json = "{\"result\":\"ERROR\",\"data\":{\"message\":\"Too many of you in this world\"}}";
        String result = JsonHandler.formatResponse(json);

        assertEquals("Too many of you in this world", result);
    }

    @Test
    void formatResponse_turnMessage_returnsMessageOnly() {
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"Turned left\"}}";
        String result = JsonHandler.formatResponse(json);

        assertEquals("Turned left", result);
    }

    // formatResponse() tests — state responses (key-value pairs)
    @Test
    void formatResponse_stateResponse_containsPosition() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":\"5,3\",\"direction\":\"NORTH\",\"shields\":3,\"shots\":3,\"status\":\"NORMAL\"}}";
        String result = JsonHandler.formatResponse(json);

        assertTrue(result.contains("position: 5,3"));
    }

    @Test
    void formatResponse_stateResponse_formatsArrayPosition() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":[5,3]}}";

        assertTrue(JsonHandler.formatResponse(json).contains("position: [5,3]"));
    }

    @Test
    void formatResponse_stateResponse_containsDirection() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":\"5,3\",\"direction\":\"NORTH\",\"shields\":3,\"shots\":3,\"status\":\"NORMAL\"}}";
        String result = JsonHandler.formatResponse(json);

        assertTrue(result.contains("direction: NORTH"));
    }

    @Test
    void formatResponse_stateResponse_containsShields() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":\"5,3\",\"direction\":\"NORTH\",\"shields\":3,\"shots\":3,\"status\":\"NORMAL\"}}";
        String result = JsonHandler.formatResponse(json);

        assertTrue(result.contains("shields: 3"));
    }

    @Test
    void formatResponse_stateResponse_containsShots() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":\"5,3\",\"direction\":\"NORTH\",\"shields\":3,\"shots\":3,\"status\":\"NORMAL\"}}";
        String result = JsonHandler.formatResponse(json);

        assertTrue(result.contains("shots: 3"));
    }

    @Test
    void formatResponse_stateResponse_containsStatus() {
        String json = "{\"result\":\"OK\",\"data\":{\"position\":\"5,3\",\"direction\":\"NORTH\",\"shields\":3,\"shots\":3,\"status\":\"NORMAL\"}}";
        String result = JsonHandler.formatResponse(json);

        assertTrue(result.contains("status: NORMAL"));
    }

    // formatResponse() tests — look responses (array)
    @Test
    void formatResponse_lookResponse_printsSightingsOnePerLine() {
        String json = "{\"result\":\"OK\",\"data\":{\"objects\":[\"NORTH: Wall at distance 2\",\"SOUTH: Nothing visible\"]}}";
        String result = JsonHandler.formatResponse(json);

        assertTrue(result.contains("NORTH: Wall at distance 2"));
        assertTrue(result.contains("SOUTH: Nothing visible"));
    }

    @Test
    void formatResponse_lookResponse_doesNotContainRawJson() {
        String json = "{\"result\":\"OK\",\"data\":{\"objects\":[\"NORTH: Wall at distance 2\"]}}";
        String result = JsonHandler.formatResponse(json);

        assertFalse(result.contains("{"));
        assertFalse(result.contains("}"));
    }

    @Test
    void formatResponse_emptySightings_returnsEmptyOrTrimmed() {
        String json = "{\"result\":\"OK\",\"data\":{\"objects\":[]}}";
        String result = JsonHandler.formatResponse(json);

        assertNotNull(result);
        assertEquals("", result.trim());
    }

    // formatResponse() tests — edge cases
    @Test
    void formatResponse_invalidJson_returnsRawInput() {
        String notJson = "this is not json";
        String result = JsonHandler.formatResponse(notJson);

        assertEquals(notJson, result);
    }

    @Test
    void formatResponse_nullData_returnsRawJson() {
        String json = "{\"result\":\"OK\"}";
        String result = JsonHandler.formatResponse(json);

        assertEquals(json, result);
    }

    @Test
    void formatResponse_emptyString_returnsEmptyString() {
        String result = JsonHandler.formatResponse("");
        assertEquals("", result);
    }
}
