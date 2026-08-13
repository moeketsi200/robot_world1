package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.Server;
import za.co.wethinkcode.robots.server.domain.*;

import java.net.Socket;

import za.co.wethinkcode.robots.protocol.CommandRequest;

// Brief: Base interface for server commands; implementors execute actions for a request.
public interface Command {
    String execute(CommandRequest request, Socket clientSocket);
}
//  Base interface for all commands, defines the execute method that takes a CommandRequest and client Socket, and returns a response String.   
