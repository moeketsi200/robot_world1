package za.co.wethinkcode.robots.server;

import com.fasterxml.jackson.databind.JsonNode;

public interface RobotWorldClient {
    void connect(String ip, int port);
    void disconnect();
    boolean isConnected();
    JsonNode sendRequest(String jsonRequest);
}