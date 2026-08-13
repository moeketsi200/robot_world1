package za.co.wethinkcode.robots.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;

// Brief: Representation of a server response message used by the protocol.
public class ServerResponse {
    private String result;
    private Map<String, Object> data = new LinkedHashMap<>();

    public ServerResponse() {
    }

    public ServerResponse(String result, String message) {
        this.result = result;
        this.data.put("message", message);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @JsonIgnore
    public String getMessage() {
        Object message = data == null ? null : data.get("message");
        return message == null ? "" : message.toString();
    }
}
