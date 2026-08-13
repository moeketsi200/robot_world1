package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class QuitCommandTest {

    @Test
    public void executeCallsShutdownOnServer() {
        class TestServer extends Server {
            boolean shutdownCalled = false;

            public TestServer() {
                super(0);
            }

            @Override
            public void shutdown() {
                shutdownCalled = true;
                // Do not call super.shutdown() to avoid System.exit during tests
            }
        }

        TestServer server = new TestServer();
        QuitCommand cmd = new QuitCommand(server);
        cmd.execute();

        assertTrue(server.shutdownCalled, "QuitCommand.execute() should call Server.shutdown()");
    }
}
