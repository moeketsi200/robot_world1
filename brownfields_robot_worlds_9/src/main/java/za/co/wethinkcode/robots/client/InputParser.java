package za.co.wethinkcode.robots.client;

public class InputParser {

    private String[] parts;
    private String error;

    public InputParser(String input) {
        parse(input);
    }

    public boolean isValid() {
        return error == null;
    }

    public String getError() {
        return error;
    }

    public String getCommand() {
        return parts[0].toLowerCase();
    }

    public String[] getParts() {
        return parts;
    }

    public String getArg(int index) {
        if (index >= parts.length) return null;
        return parts[index];
    }

    /**
     * Returns which robot this command applies to.
     * launch <make> <name>   -> name is parts[2]
     * quit                   -> no robot, ends the whole session
     * everything else        -> <command> <robot> ...  -> parts[1]
     */
    public String getRobotName() {
        String command = getCommand();
        if (command.equals("launch")) {
            return parts.length > 2 ? parts[2] : null;
        }
        if (command.equals("quit")) {
            return null;
        }
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * Returns the command's own arguments, with the leading command word
     * and (for non-launch commands) the robot name stripped out.
     * e.g. "forward HAL 5" -> ["5"]
     *      "launch Scout HAL" -> ["Scout", "HAL"]   (unchanged - LaunchCommand needs both)
     */
    public String[] getCommandArgs() {
        String command = getCommand();
        if (command.equals("launch") || command.equals("quit")) {
            return java.util.Arrays.copyOfRange(parts, 1, parts.length);
        }
        int from = Math.min(2, parts.length);
        return java.util.Arrays.copyOfRange(parts, from, parts.length);
    }

    private void parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            error = "Please enter a command.";
            return;
        }

        parts = input.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "launch":
                checkLaunch();
                break;
            case "forward":
                checkMovement("forward");
                break;
            case "back":
                checkMovement("back");
                break;
            case "turn":
                checkTurn();
                break;
            case "look":
            case "fire":
            case "repair":
            case "reload":
            case "state":
                checkRobotOnly(command);
                break;
            case "quit":
                checkNoArguments("quit");
                break;
            default:
                error = "'" + parts[0] + "' is not a valid command. "
                        + "Valid commands: launch, forward, back, turn, "
                        + "look, fire, repair, reload, state, quit";
                break;
        }
    }

    private void checkLaunch() {
        if (parts.length < 3) {
            error = "Usage: launch <make> <name>"
                    + "\nExample: launch Scout Hal";
            return;
        }
        if (parts[1].isEmpty() || parts[2].isEmpty()) {
            error = "The make and name cannot be blank.";
        }
    }

    // forward <robot> <steps>  /  back <robot> <steps>
    private void checkMovement(String command) {
        if (parts.length < 3) {
            error = "Usage: " + command + " <robot> <steps>"
                    + "\nExample: " + command + " Hal 5";
            return;
        }

        int steps;
        try {
            steps = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            error = "'" + parts[2] + "' is not a valid number of steps."
                    + "\nUsage: " + command + " <robot> <steps>  (e.g. " + command + " Hal 3)";
            return;
        }

        if (steps <= 0) {
            error = "Steps must be a positive number greater than zero. Got: " + steps;
        }
    }

    // turn <robot> <left|right>
    private void checkTurn() {
        if (parts.length < 3) {
            error = "Usage: turn <robot> <left|right>"
                    + "\nExample: turn Hal left";
            return;
        }

        String direction = parts[2].toLowerCase();
        if (!direction.equals("left") && !direction.equals("right")) {
            error = "'" + parts[2] + "' is not a valid direction."
                    + "\nUsage: turn <robot> <left|right>";
        }
    }

    // look <robot> / fire <robot> / repair <robot> / reload <robot> / state <robot>
    private void checkRobotOnly(String command) {
        if (parts.length < 2) {
            error = "Usage: " + command + " <robot>"
                    + "\nExample: " + command + " Hal";
            return;
        }
        if (parts.length > 2) {
            error = "'" + command + "' takes only a robot name."
                    + "\nUsage: " + command + " <robot>";
        }
    }

    private void checkNoArguments(String command) {
        if (parts.length > 1) {
            error = "'" + command + "' does not take any arguments."
                    + "\nUsage: " + command;
        }
    }
}
//this is a tests