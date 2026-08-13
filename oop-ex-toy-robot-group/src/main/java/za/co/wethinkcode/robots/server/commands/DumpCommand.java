package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

// Brief: Command to dump or display the current world state (invoked from server console).
public class DumpCommand {
    private final Server server;

    public DumpCommand(Server server){
        this.server = server;
    }

    // for now its shutdown i dont have dumpworld in the server yet 
    public void execute(){
        server.dumpWorld();
    }
    
}
