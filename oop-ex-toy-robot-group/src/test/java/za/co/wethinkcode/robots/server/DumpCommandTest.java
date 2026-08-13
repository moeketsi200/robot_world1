package za.co.wethinkcode.robots.server;

import za.co.wethinkcode.robots.server.domain.*;
import za.co.wethinkcode.robots.server.commands.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DumpCommandTest {

    @Test
    public void executeCallsDumpWorldOnServer() {
        class TestServer extends Server {
            boolean dumped = false;

            public TestServer() {
                super(0);
            }

            @Override
            public void dumpWorld() {
                dumped = true;
            }
        }

        TestServer server = new TestServer();
        DumpCommand cmd = new DumpCommand(server);
        cmd.execute();

        assertTrue(server.dumped, "DumpCommand.execute() should call Server.dumpWorld()");
    }
}
