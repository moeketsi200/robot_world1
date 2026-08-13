package za.co.wethinkcode.robots.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RobotWorldJsonClient implements RobotWorldClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to server at " + ip + ":" + port, e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while disconnecting", e);
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public JsonNode sendRequest(String jsonRequest) {
        try {
            out.println(jsonRequest);
            String response = in.readLine();
            if (response == null) {
                throw new RuntimeException("Server closed the connection with no response");
            }
            return mapper.readTree(response);
        } catch (IOException e) {
            throw new RuntimeException("Error communicating with server", e);
        }
    }
}