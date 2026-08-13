package za.co.wethinkcode.robots.server.commands;
import java.util.ArrayList;

import za.co.wethinkcode.robots.server.Robot;
import za.co.wethinkcode.robots.server.world.World;

public class PurgeCommand {
    
    
    /*
        This method removes all robots from the world. 
        It first retrieves all robots currently in the world using the getAllRobots() method. 
        If there are no robots connected, it prints a message indicating that there are no robots connected
        Otherwise, it iterates through each robot and removes them from the world using the removeRobot() method.
    
    */
            public String purgeRobots(World world) {
                var robots = new ArrayList<>(world.getAllRobots());
            
                if (robots.isEmpty()) {
                    System.out.println("  (no robots connected)");
                    return "No robots connected";
                }
            
                for (Robot robot : robots) {
                    /*
                    This for-each loop iterates through each robot in the list of robots retrieved from the world.
                    For each robot, it calls the removeRobot() method of the world object, passing the robot's name and a boolean value true as arguments. 
                    This effectively removes the robot from the world. After removing the robot, it prints a message indicating that the robot has been removed from the
                    */
                    world.removeRobot(robot.getName(), true);
                    System.out.println("Robot : " + robot.getName()+ " has been removed from the world.");
                }
                return "Robots purged successfully";
            }
 }
    



