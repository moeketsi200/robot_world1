package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

// Brief: Command to cleanly shut down the server or disconnect a client session.
public class QuitCommand {
    private final Server server;

    public QuitCommand(Server server) {
        this.server = server;
    }

    public void execute() {
        server.shutdown();
    }
}
