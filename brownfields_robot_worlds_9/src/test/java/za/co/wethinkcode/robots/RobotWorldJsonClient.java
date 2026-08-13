package za.co.wethinkcode.robots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RobotWorldJsonClient implements RobotWorldClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Socket socket;
    private PrintStream out;
    private BufferedReader in;

    @Override
    public void connect(String ipAddress, int port) {
        try {
            socket = new Socket(ipAddress, port);
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            // Consume the initial welcome message sent by the server upon connecting (if any)
            socket.setSoTimeout(300);
            try {
                in.readLine();
            } catch (IOException ignored) {
                // Server doesn't send a welcome message; proceed
            }
            socket.setSoTimeout(0);
        } catch (IOException e) {
            // error connecting should just throw Runtime error and fail test
            throw new RuntimeException("Error connecting to Robot Worlds server.", e);
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // error connecting should just throw Runtime error and fail test
            throw new RuntimeException("Error disconnecting from Robot Worlds server.", e);
        }
    }

    @Override
    public JsonNode sendRequest(String requestJsonString) {
        try {
            out.println(requestJsonString);
            out.flush();
            return OBJECT_MAPPER.readTree(in.readLine());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing server response as JSON.", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading server response.", e);
        }
    }

    @Override
    public String sendRequestAsString(String requestString) {
        try {
            out.println(requestString);
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error reading server response.", e);
        }
    }

    private static final int PUSH_NOTIFICATION_TIMEOUT_MS = 10000;


    /*
    This method blocks and waits for a server-initiated notification to be pushed to the client.
    It is used to receive notifications such as "you've been removed from the world" when
    the server purges a robot. The method will wait for a maximum of PUSH_NOTIFICATION_TIMEOUT_MS milliseconds for a notification to arrive before throwing an exception.
    If a notification is received, it is parsed as a JsonNode and returned. If the server closes the connection without sending a notification,
    or if the notification cannot be parsed as JSON, a RuntimeException is thrown.
    
    */
@Override
public JsonNode readPushedNotification() {
    try {
        socket.setSoTimeout(PUSH_NOTIFICATION_TIMEOUT_MS);
        String line = in.readLine();
        if (line == null) {
            throw new RuntimeException("Server closed the connection with no notification.");
        }
        return OBJECT_MAPPER.readTree(line);
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Error parsing pushed notification as JSON.", e);
    } catch (IOException e) {
        throw new RuntimeException(
                "No pushed notification received within " + PUSH_NOTIFICATION_TIMEOUT_MS + "ms.", e);
    } finally {
        try {
            socket.setSoTimeout(0);
        } catch (IOException ignored) {
            // socket likely already closed; nothing more to do
        }
    }
}

@Override
public JsonNode notifyRemoved() {
    return readPushedNotification();
}
}
