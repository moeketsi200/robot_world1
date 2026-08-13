package za.co.wethinkcode.robots.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.Glow;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.effect.DropShadow;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.FadeTransition;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import za.co.wethinkcode.robots.protocol.JsonProtocol;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * RobotGuiApp serves as the primary Graphical User Interface client for the Robot World game.
 * It connects to the server via sockets, sends JSON commands, and visually renders 
 * the 2D world map, obstacles, robots, and animated laser fire.
 */
public class RobotGuiApp extends Application {

    private TextArea console;
    private Group worldGroup;
    private Group laserGroup;

    public static class RobotState {
        public int x;
        public int y;
        public String direction;
        public String kind;
    }

    private final Map<String, RobotState> robotStates = new HashMap<>();

    // Track robots currently rendered so we can add/remove them as server state changes.
    private final Map<String, Group> activeRobots = new HashMap<>();
    private final Map<String, RobotViewState> activeRobotStates = new HashMap<>();

    // Track obstacles currently rendered so we can refresh them when server world state changes.
    // Key format: "<type>@<x>,<y>".
    private final Map<String, Group> activeObstacles = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PrintStream out;
    private boolean radarPingStarted = false;

    static final Pattern FIRE_HIT_PATTERN = Pattern.compile("\\[(.+?)] Fired! Hit .*? at \\[(-?\\d+),\\s*(-?\\d+)] \\(Distance: (\\d+)\\).*", Pattern.CASE_INSENSITIVE);
    static final Pattern FIRE_MISS_PATTERN = Pattern.compile("\\[(.+?)] Fired! Missed\\.", Pattern.CASE_INSENSITIVE);

    /**
     * Entry point for the JavaFX application. Initializes the main layout,
     * builds the 2D scene, sets up the console output, and establishes the network connection.
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        ScrollPane scene2D = create2dScene();
        TextArea consoleArea = createConsole();
        
        // 1. Game Board (Center)
        root.setCenter(scene2D);
        
        // 2. Output Console (Bottom) - Reverted to purely display messages
        root.setBottom(consoleArea);

        Scene scene = new Scene(root, 1000, 800);

        primaryStage.setTitle("Robot World Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Get IP address from system property first, then raw arguments, defaulting to localhost
        String host = System.getProperty("server.host");
        if (host == null || host.isBlank()) {
            List<String> rawParams = getParameters().getRaw();
            if (rawParams.isEmpty() || rawParams.get(0).isBlank()) {
                host = "localhost";
            } else {
                host = rawParams.get(0);
            }
        }

        // Start the background network thread to connect to the server
        connectToServer(host);
        
        // Start reading your typing from the terminal
        startTerminalInput();
    }

    /**
     * Creates the main scrollable 2D map view.
     * Generates dynamic obstacles based on the configuration and sets up the background.
     * @return A ScrollPane containing the interactive map.
     */
    private ScrollPane create2dScene() {
        worldGroup = new Group();

        final int SCALE = 10; // Zoom out by reducing the scale factor
        WorldConfig config = loadConfig("config.json");

        // 1. DYNAMIC FLOOR SIZE: Read width/height from config
        int floorW = (config != null && config.width > 0) ? config.width * SCALE : 2000;
        int floorH = (config != null && config.height > 0) ? config.height * SCALE : 2000;

        // --- 2D Floor ---
        Rectangle floor = new Rectangle(floorW, floorH);
        floor.setX(-floorW / 2.0);
        floor.setY(-floorH / 2.0);
        try {
            Image bgImage = new Image(RobotGuiApp.class.getResourceAsStream("/images/images.jpeg"));
            if (bgImage.isError()) {
                System.err.println("Could not load background image.");
                floor.setFill(Color.rgb(40, 40, 45)); // Fallback color
            } else {
                floor.setFill(new ImagePattern(bgImage, 0, 0, 400, 400, false));
            }
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
            floor.setFill(Color.rgb(40, 40, 45)); // Fallback color
        }
        worldGroup.getChildren().add(floor);

        // 2. DYNAMIC OBSTACLES (2D)
        if (config != null && config.obstacles != null) {
            Image rockImage = null;
            try {
                Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Rock.png"));
                if (!img.isError()) {
                    rockImage = img;
                }
            } catch (Exception e) {
                System.err.println("Could not load Rock.png: " + e.getMessage());
            }
            
            Image mountainImage = null;
            try {
                Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Mountain.png"));
                if (!img.isError()) {
                    mountainImage = img;
                }
            } catch (Exception e) {
                System.err.println("Could not load Mountain.png: " + e.getMessage());
            }

            Image lakeImage = null;
            try {
                Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Lake.png"));
                if (!img.isError()) {
                    lakeImage = img;
                }
            } catch (Exception e) {
                System.err.println("Could not load Lake.png: " + e.getMessage());
            }

            Image pitImage = null;
            try {
                Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Pit.png"));
                if (!img.isError()) {
                    pitImage = img;
                }
            } catch (Exception e) {
                System.err.println("Could not load Pit.png: " + e.getMessage());
            }

            for (ObstacleConfig obs : config.obstacles) {
                // Dynamically generate a unique color based on the obstacle's name!
                double hue = Math.abs(obs.type.hashCode()) % 360.0;
                Color obsColor = Color.hsb(hue, 0.8, 0.8);
                
                boolean isPit = obs.type.toLowerCase().contains("pit");
                boolean isRock = obs.type.toLowerCase().contains("rock");
                boolean isMountain = obs.type.toLowerCase().contains("mountain");
                boolean isLake = obs.type.toLowerCase().contains("lake");
                boolean isMine = obs.type.equalsIgnoreCase("Mine");
                
                // Keep obstacles easy to see on the large world map.
                int obsSize = isMine ? 20 : 60;


                Rectangle obstacleShape = new Rectangle(obsSize, obsSize);
                if (isPit && pitImage != null) {
                    obstacleShape.setFill(new ImagePattern(pitImage));
                } else if (isPit) {
                    obstacleShape.setFill(Color.RED);
                } else if (isRock && rockImage != null) {
                    obstacleShape.setFill(new ImagePattern(rockImage));
                } else if (isMountain && mountainImage != null) {
                    obstacleShape.setFill(new ImagePattern(mountainImage));
                } else if (isLake && lakeImage != null) {
                    obstacleShape.setFill(new ImagePattern(lakeImage));
                } else {
                    obstacleShape.setFill(obsColor);
                }
                
                // Center the larger obstacles perfectly on their coordinates
                double offset = (obsSize - SCALE) / 2.0;
                obstacleShape.setTranslateX((obs.x * SCALE) - offset);
                obstacleShape.setTranslateY((-obs.y * SCALE) - offset);
                
                // Add a floating text label above the obstacle
                Text obsLabel = new Text(obs.type);
                obsLabel.setFill(Color.WHITE);
                obsLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
                obsLabel.setTranslateX(obs.x * SCALE);
                obsLabel.setTranslateY((-obs.y * SCALE) - offset - 2); 
                
                worldGroup.getChildren().addAll(obstacleShape, obsLabel);
            }
        }

        // Laser visuals are rendered in their own layer so they can appear above the floor and world obstacles.
        laserGroup = new Group();
        worldGroup.getChildren().add(laserGroup);

        ScrollPane scrollPane = new ScrollPane(worldGroup);
        scrollPane.setPannable(true); // Allows you to click and drag to view the world
        scrollPane.setStyle("-fx-background: #14141E; -fx-background-color: #14141E;");
        Platform.runLater(() -> {
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
        });
        
        return scrollPane;
    }

    private TextArea createConsole() {
        console = new TextArea();
        console.setEditable(false); // The user shouldn't type here, it's just for output
        console.setPrefHeight(100);
        console.setText("Waiting for server connection...\n");
        return console;
    }

    private void sendCommand(String json) {
        if (out != null) {
            // Send JSON string to the server
            out.println(json); 
            out.flush();
            console.appendText("Sent: " + json + "\n");
        } else {
            console.appendText("Cannot send command. Not connected to server.\n");
        }
    }

    /**
     * Initializes the socket connection to the server in a background thread.
     * It continually listens for JSON responses and triggers UI updates.
     * It also manages the background radar ping which polls for robot states.
     * @param host The server hostname or IP address.
     */
    private void connectToServer(String host) {
        Thread networkThread = new Thread(() -> {
            try {
                String targetHost = host;
                int targetPort = 5000;
                if (host.contains(":")) {
                    String[] parts = host.split(":");
                    targetHost = parts[0];
                    try {
                        targetPort = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Invalid port in host. Using default port 5000.");
                    }
                }

                Socket socket = new Socket(targetHost, targetPort);
                PrintStream output = new PrintStream(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                this.out = output; // Assign to instance variable so buttons can use it
                
                final int finalPort = targetPort;
                Platform.runLater(() -> console.appendText("Connected to server at " + socket.getInetAddress().getHostAddress() + " on port " + finalPort + "!\n"));
                
                String response;
                // Loop continuously to read any incoming messages from the server
                while ((response = input.readLine()) != null) {
                    final String rawResponse = response;
                    
                    try {
                        JsonNode root = objectMapper.readTree(rawResponse);
                        
                        // Pass the whole JSON object to our smarter extractor
                        // Update both world obstacles (if present) and robots.
                        // Obstacles may arrive in server payloads, while robots arrive from the radar ping.
                        syncObstaclesFromServer(root);
                        extractAndDrawAllRobots(root);
                        processFireEvent(root);
                        
                        // Only print to the GUI console if it's not a raw JSON array from the background radar ping
                        if (!rawResponse.trim().startsWith("[")) {
                            final String formattedMessage = JsonProtocol.messageFromResponseLine(response);
                            Platform.runLater(() -> {
                                console.appendText("Server: " + formattedMessage + "\n");
                                triggerLaserEffectIfFired(formattedMessage);
                            });
                        }
                        
                    } catch (Exception ex) {
                        // Fallback just in case JSON parsing fails
                        Platform.runLater(() -> console.appendText("Server: " + rawResponse + "\n"));
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    console.appendText("Connection failed: Is your server running?\n");
                    // Ask user for a different IP address/host using a dialog
                    javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(host);
                    dialog.setTitle("Connect to Remote Server");
                    dialog.setHeaderText("Could not connect to server at " + host + " on port 5000.");
                    dialog.setContentText("Please enter the server IP address / hostname (e.g. 192.168.1.100 or 192.168.1.100:5000):");
                    
                    // Add modern sleek dark mode styling to the dialog pane
                    javafx.scene.control.DialogPane pane = dialog.getDialogPane();
                    pane.setStyle("-fx-background-color: #1E1E2E; -fx-text-fill: #FFFFFF;");
                    pane.lookup(".label").setStyle("-fx-text-fill: #FFFFFF;");
                    
                    java.util.Optional<String> result = dialog.showAndWait();
                    if (result.isPresent() && !result.get().trim().isEmpty()) {
                        String newHost = result.get().trim();
                        console.appendText("Retrying connection to " + newHost + "...\n");
                        connectToServer(newHost);
                    }
                });
            }
        });
        networkThread.setDaemon(true); // Thread will close automatically when the GUI window is closed
        networkThread.start();
        
        // Start the background Radar Ping to ask for robot positions every 2 seconds
        synchronized (this) {
            if (!radarPingStarted) {
                radarPingStarted = true;
                Thread radarPing = new Thread(() -> {
                    while (true) {
                        try { Thread.sleep(2000); } catch (InterruptedException e) { break; }
                        // Send a silent request for the robot list
                        PrintStream currentOut = this.out;
                        if (currentOut != null) { 
                            currentOut.println("{\"robot\":\"\",\"command\":\"robots\",\"arguments\":[]}"); 
                            currentOut.flush(); 
                        }
                    }
                });
                radarPing.setDaemon(true);
                radarPing.start();
            }
        }
    }

    private void syncObstaclesFromServer(JsonNode root) {
        try {
            // Attempt to locate world/obstacle objects in server payload.
            JsonNode objectsNode = null;

            // Common candidates (depending on server response format)
            if (root != null) {
                if (root.has("objects") && root.get("objects").isArray()) {
                    objectsNode = root.get("objects");
                } else if (root.has("world") && root.get("world").has("objects") && root.get("world").get("objects").isArray()) {
                    objectsNode = root.get("world").get("objects");
                } else if (root.has("state") && root.get("state").has("objects") && root.get("state").get("objects").isArray()) {
                    objectsNode = root.get("state").get("objects");
                } else if (root.has("data") && root.get("data").has("objects") && root.get("data").get("objects").isArray()) {
                    objectsNode = root.get("data").get("objects");
                } else if (root.has("data") && root.get("data").isArray()) {
                    // Some implementations return a list payload under data.
                    objectsNode = root.get("data");
                }
            }

            // If we can't find obstacles in the payload, do nothing (keeps config-based obstacles as fallback).
            if (objectsNode == null || !objectsNode.isArray()) {
                return;
            }

            final JsonNode finalObjects = objectsNode;
            final int SCALE = 10;

            Platform.runLater(() -> {
                try {
                    // Rebuild obstacle scene from scratch for correctness.
                    // Clear previously rendered server-driven obstacles.
                    for (Group g : activeObstacles.values()) {
                        worldGroup.getChildren().remove(g);
                    }
                    activeObstacles.clear();

                    // Load textures lazily (only if needed)
                    Image rockImage = null;
                    Image mountainImage = null;
                    Image lakeImage = null;

                    try {
                        Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Rock.png"));
                        if (!img.isError()) rockImage = img;
                    } catch (Exception ignored) {
                    }
                    try {
                        Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Mountain.png"));
                        if (!img.isError()) mountainImage = img;
                    } catch (Exception ignored) {
                    }
                    try {
                        Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Lake.png"));
                        if (!img.isError()) lakeImage = img;
                    } catch (Exception ignored) {
                    }
                    Image pitImage = null;
                    try {
                        Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Pit.png"));
                        if (!img.isError()) pitImage = img;
                    } catch (Exception ignored) {
                    }
                    Image mineImage = null;
                    try {
                        Image img = new Image(RobotGuiApp.class.getResourceAsStream("/images/Mine.png"));
                        if (!img.isError()) mineImage = img;
                    } catch (Exception ignored) {
                    }

                    for (JsonNode oNode : finalObjects) {
                        int ox = oNode.path("x").asInt();
                        int oy = oNode.path("y").asInt();
                        String type = oNode.path("type").asText("");

                        // Some server payloads may use different field names
                        if (type.isBlank() && oNode.has("kind")) {
                            type = oNode.path("kind").asText("");
                        }

                        // Skip invalid entries
                        if (type.isBlank()) {
                            continue;
                        }

                        String key = type + "@" + ox + "," + oy;

                        boolean isPit = type.toLowerCase().contains("pit");
                        boolean isRock = type.toLowerCase().contains("rock");
                        boolean isMountain = type.toLowerCase().contains("mountain");
                        boolean isLake = type.toLowerCase().contains("lake");
                        boolean isMine = type.equalsIgnoreCase("Mine");

                        // Keep obstacles easy to see on the large world map.
                        int obsSize = isMine ? 20 : 60;

                        double offset = (obsSize - SCALE) / 2.0;


                        Rectangle obstacleShape = new Rectangle(obsSize, obsSize);
                        if (isPit && pitImage != null) {
                            obstacleShape.setFill(new ImagePattern(pitImage));
                        } else if (isPit) {
                            obstacleShape.setFill(Color.RED);
                        } else if (isRock && rockImage != null) {
                            obstacleShape.setFill(new ImagePattern(rockImage));
                        } else if (isMountain && mountainImage != null) {
                            obstacleShape.setFill(new ImagePattern(mountainImage));
                        } else if (isLake && lakeImage != null) {
                            obstacleShape.setFill(new ImagePattern(lakeImage));
                        } else if (isMine && mineImage != null) {
                            obstacleShape.setFill(new ImagePattern(mineImage));
                        } else {
                            double hue = Math.abs(type.hashCode()) % 360.0;
                            Color obsColor = Color.hsb(hue, 0.8, 0.8);
                            obstacleShape.setFill(obsColor);
                        }

                        obstacleShape.setTranslateX((ox * SCALE) - offset);
                        obstacleShape.setTranslateY((-oy * SCALE) - offset);

                        Text obsLabel = new Text(type);
                        obsLabel.setFill(Color.WHITE);
                        obsLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
                        obsLabel.setTranslateX(ox * SCALE);
                        obsLabel.setTranslateY((-oy * SCALE) - offset - 2);

                        Group obstacleGroup = new Group(obstacleShape, obsLabel);
                        activeObstacles.put(key, obstacleGroup);
                        worldGroup.getChildren().add(obstacleGroup);
                    }
                } catch (Exception ex) {
                    System.err.println("GUI obstacle sync error: " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            // Ignore payload variations; keep existing obstacles.
        }
    }


    private void extractAndDrawAllRobots(JsonNode root) {
        try {
            JsonNode robotsArray = null;

            // 1. Hunt for the array
            if (root.isArray()) {
                robotsArray = root; // It's a raw array
            } else if (root.has("robots") && root.get("robots").isArray()) {
                robotsArray = root.get("robots"); // Wrapped in {"robots": [...]}
            } else if (root.has("state") && root.get("state").has("robots")) {
                robotsArray = root.get("state").get("robots"); // Wrapped in state object
            } else if (root.has("data") && root.get("data").isArray()) {
                robotsArray = root.get("data"); // Wrapped in a data object
            }

            // 2. If we found an array, draw the robots!
            if (robotsArray != null && robotsArray.isArray()) {
                final JsonNode finalArray = robotsArray;
                final int SCALE = 10; // Match the new zoomed out scale

                Platform.runLater(() -> {
                  try {
                    HashSet<String> robotsOnScreen = new HashSet<>(activeRobots.keySet());

                    for (JsonNode rNode : finalArray) {
                        String name = rNode.path("name").asText("Unknown");
                        
                        // Handle server returning coordinates as an array [x,y] or separate fields
                        int x = 0, y = 0;
                        if (rNode.has("position") && rNode.get("position").isArray()) {
                            x = rNode.get("position").get(0).asInt(0);
                            y = rNode.get("position").get(1).asInt(0);
                        } else {
                            x = rNode.path("x").asInt(0);
                            y = rNode.path("y").asInt(0);
                        }
                        
                        String direction = rNode.path("direction").asText("NORTH");
                        String kind = rNode.path("kind").asText("normal");

                        RobotState robotState = robotStates.computeIfAbsent(name, k -> new RobotState());
                        robotState.x = x;
                        robotState.y = y;
                        robotState.direction = direction;
                        robotState.kind = kind;

                        // Extract shields and shots safely (defaults to 5 if not found)
                        int shields = 5, shots = 5;
                        if (rNode.has("shields")) shields = rNode.path("shields").asInt(5);
                        else if (rNode.has("state") && rNode.get("state").has("shields")) shields = rNode.get("state").path("shields").asInt(5);
                        
                        if (rNode.has("shots")) shots = rNode.path("shots").asInt(5);
                        else if (rNode.has("state") && rNode.get("state").has("shots")) shots = rNode.get("state").path("shots").asInt(5);

                        robotsOnScreen.remove(name); // Mark this robot as "seen"

                        boolean isNew = false;
                        Group robotModel = activeRobots.get(name);
                        if (robotModel == null) {
                            // This is a new robot, create its 3D model
                            robotModel = createRobot(name, kind);
                            activeRobots.put(name, robotModel);
                            worldGroup.getChildren().add(robotModel);
                            isNew = true;
                        }

                        activeRobotStates.put(name, new RobotViewState(x, y, direction, kind));

                        // Update health and ammo bars dynamically! (Assuming max of 5)
                        Rectangle hpBar = (Rectangle) robotModel.lookup("#hpBar");
                        if (hpBar != null) hpBar.setWidth(Math.min(40, Math.max(0, shields * 8)));
                        
                        Rectangle ammoBar = (Rectangle) robotModel.lookup("#ammoBar");
                        if (ammoBar != null) ammoBar.setWidth(Math.min(40, Math.max(0, shots * 8)));

                        double targetAngle = switch (direction.toUpperCase()) {
                            case "EAST" -> 90;
                            case "SOUTH" -> 180;
                            case "WEST" -> 270;
                            default -> 0; // NORTH
                        };
                        
                        if (isNew) {
                            robotModel.setTranslateX(x * SCALE);
                            robotModel.setTranslateY(-y * SCALE); 
                            ImageView imgView = (ImageView) robotModel.lookup("#imageView");
                            if (imgView != null) imgView.setRotate(targetAngle);
                        } else {
                            // Animate movement to the new coordinate
                            TranslateTransition tt = new TranslateTransition(Duration.millis(300), robotModel);
                            tt.setToX(x * SCALE);
                            tt.setToY(-y * SCALE);
                            tt.play();

                            // Animate rotation along the shortest path
                            ImageView imgView = (ImageView) robotModel.lookup("#imageView");
                            if (imgView != null) {
                                double currentAngle = imgView.getRotate();
                                double diff = (targetAngle - currentAngle) % 360;
                                if (diff > 180) diff -= 360;
                                if (diff < -180) diff += 360;
                                
                                if (Math.abs(diff) > 0.1) {
                                    RotateTransition rt = new RotateTransition(Duration.millis(200), imgView);
                                    rt.setByAngle(diff);
                                    rt.play();
                                }
                            }
                        }
                    }

                    for (String nameToRemove : robotsOnScreen) {
                        Group robotToRemove = activeRobots.remove(nameToRemove);
                        activeRobotStates.remove(nameToRemove);
                        if (robotToRemove != null) {
                            worldGroup.getChildren().remove(robotToRemove);
                        }
                        robotStates.remove(nameToRemove);
                    }
                  } catch (Exception ex) {
                      System.err.println("GUI Thread Error: " + ex.getMessage());
                  }
                });
            }
        } catch (Exception e) {
            System.err.println("Error rendering robots: " + e.getMessage());
        }
    }

    private void triggerLaserEffectIfFired(String message) {
        if (message == null || message.isBlank()) return;

        String[] lines = message.split("\\r?\\n");
        java.util.regex.Pattern firePattern = java.util.regex.Pattern.compile("^\\s*\\[([^\\]]+)\\] Fired! (.*)$");
        java.util.regex.Pattern hitPattern = java.util.regex.Pattern.compile("^Hit .*? at \\[(-?\\d+),\\s*(-?\\d+)\\] \\(Distance: (\\d+)\\).*");

        final int SCALE = 10;

        for (String line : lines) {
            java.util.regex.Matcher matcher = firePattern.matcher(line.trim());
            if (!matcher.matches()) {
                continue;
            }

            String shooterName = matcher.group(1);
            String details = matcher.group(2);

            RobotState state = robotStates.get(shooterName);
            if (state == null) {
                continue;
            }

            double startX = state.x * SCALE;
            double startY = -state.y * SCALE;
            double endX;
            double endY;

            if (details.startsWith("Missed")) {
                int bulletDistance = switch (state.kind != null ? state.kind.toLowerCase() : "normal") {
                    case "sniper" -> 10;
                    case "heavy" -> 3;
                    case "bomber" -> 4;
                    default -> 5;
                };

                int dx = 0;
                int dy = 0;
                switch (state.direction.toUpperCase()) {
                    case "NORTH" -> dy = -1;
                    case "SOUTH" -> dy = 1;
                    case "EAST" -> dx = 1;
                    case "WEST" -> dx = -1;
                }

                endX = startX + dx * bulletDistance * SCALE;
                endY = startY + dy * bulletDistance * SCALE;
            } else {
                java.util.regex.Matcher hitMatcher = hitPattern.matcher(details);
                if (hitMatcher.matches()) {
                    int hitX = Integer.parseInt(hitMatcher.group(1));
                    int hitY = Integer.parseInt(hitMatcher.group(2));
                    endX = hitX * SCALE;
                    endY = -hitY * SCALE;
                } else {
                    continue;
                }
            }

            Color laserColor;
            double strokeWidth;
            switch (state.kind != null ? state.kind.toLowerCase() : "normal") {
                case "sniper" -> {
                    laserColor = Color.RED;
                    strokeWidth = 2.0;
                }
                case "heavy" -> {
                    laserColor = Color.ORANGE;
                    strokeWidth = 5.0;
                }
                case "bomber" -> {
                    laserColor = Color.YELLOW;
                    strokeWidth = 4.0;
                }
                default -> {
                    laserColor = Color.LAWNGREEN;
                    strokeWidth = 3.0;
                }
            }

            Line laserLine = new Line();
            laserLine.setStartX(startX);
            laserLine.setStartY(startY);
            laserLine.setEndX(startX);
            laserLine.setEndY(startY);
            laserLine.setStroke(laserColor);
            laserLine.setStrokeWidth(strokeWidth);
            laserLine.setStrokeLineCap(StrokeLineCap.ROUND);

            DropShadow glow = new DropShadow();
            glow.setColor(laserColor);
            glow.setRadius(8);
            glow.setSpread(0.6);
            laserLine.setEffect(glow);

            worldGroup.getChildren().add(laserLine);

            Timeline growTimeline = new Timeline(
                new KeyFrame(Duration.millis(150),
                    new KeyValue(laserLine.endXProperty(), endX),
                    new KeyValue(laserLine.endYProperty(), endY)
                )
            );

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), laserLine);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setDelay(Duration.millis(100));
            fadeTransition.setOnFinished(e -> worldGroup.getChildren().remove(laserLine));

            growTimeline.play();
            fadeTransition.play();
        }
    }

    private Group createRobot(String name, String kind) {
        Group robotGroup = new Group();

        // Load your custom robot picture!
        ImageView robotImageView = new ImageView();
        robotImageView.setId("imageView");
        try {
            String[] imageFiles = {
                "/images/robot_sprites copy.png",
                "/images/robot1.png",
                "/images/robot2.png"
            };
            // Use the robot's name to pick the image so the same name always gets 
            // the same image, and different names are likely to get different images!
            String selectedImage = imageFiles[Math.abs(name.hashCode()) % imageFiles.length];
            Image robotImage = new Image(RobotGuiApp.class.getResourceAsStream(selectedImage));
            if (robotImage.isError()) {
                System.err.println("Could not load robot image.");
            } else {
                robotImageView.setImage(robotImage);
            }
            robotImageView.setFitWidth(40); // Increased size from 20 to 40
            robotImageView.setFitHeight(40);
            // Center the image so it rotates perfectly on its exact center
            robotImageView.setTranslateX(-20);
            robotImageView.setTranslateY(-20);
        } catch (Exception e) {
            System.err.println("Could not load image: " + e.getMessage());
        }

        Text nameLabel = new Text(name);
        nameLabel.setFill(Color.WHITE);
        nameLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        nameLabel.setTranslateX(-name.length() * 3); 
        nameLabel.setTranslateY(-45); // Moved up further to make room for bars

        Rectangle hpBar = new Rectangle(40, 4, Color.LIMEGREEN);
        hpBar.setId("hpBar");
        hpBar.setTranslateX(-20);
        hpBar.setTranslateY(-38);

        Rectangle ammoBar = new Rectangle(40, 4, Color.DODGERBLUE);
        ammoBar.setId("ammoBar");
        ammoBar.setTranslateX(-20);
        ammoBar.setTranslateY(-32);

        robotGroup.getChildren().addAll(robotImageView, hpBar, ammoBar, nameLabel);
        robotGroup.setScaleX(1.2); 
        robotGroup.setScaleY(1.2);

        return robotGroup;
    }

    /**
     * Parses the server response payload to detect firing events and triggers 
     * animated laser visuals between the shooter and target.
     * @param root The root JSON node of the server response.
     */
    private void processFireEvent(JsonNode root) {
        if (root == null || !root.has("data") || !root.get("data").has("message")) {
            return;
        }

        String message = root.get("data").get("message").asText("");
        Matcher hitMatcher = FIRE_HIT_PATTERN.matcher(message);
        Matcher missMatcher = FIRE_MISS_PATTERN.matcher(message);

        if (hitMatcher.find()) {
            String shooterName = hitMatcher.group(1);
            int targetX = Integer.parseInt(hitMatcher.group(2));
            int targetY = Integer.parseInt(hitMatcher.group(3));
            drawLaserShot(shooterName, targetX, targetY);
        } else if (missMatcher.find()) {
            String shooterName = missMatcher.group(1);
            drawLaserShot(shooterName, null, null);
        }
    }

    private void drawLaserShot(String shooterName, Integer targetX, Integer targetY) {
        RobotViewState shooter = activeRobotStates.get(shooterName);
        if (shooter == null) {
            return;
        }

        int startX = shooter.x * 10;
        int startY = shooter.y * 10;
        int endX;
        int endY;

        if (targetX != null && targetY != null) {
            endX = targetX * 10;
            endY = targetY * 10;
        } else {
            int range = bulletRangeForKind(shooter.kind);
            switch (shooter.direction.toUpperCase()) {
                case "EAST" -> { endX = startX + range * 10; endY = startY; }
                case "SOUTH" -> { endX = startX; endY = startY + range * 10; }
                case "WEST" -> { endX = startX - range * 10; endY = startY; }
                default -> { endX = startX; endY = startY - range * 10; }
            }
        }

        Platform.runLater(() -> {
            if (laserGroup == null) {
                return;
            }

            Line laser = new Line(startX, startY, endX, endY);
            laser.setStroke(Color.rgb(255, 70, 120));
            laser.setStrokeWidth(4);
            laser.setStrokeLineCap(StrokeLineCap.ROUND);
            laser.setOpacity(0.95);
            laser.setEffect(new Glow(0.9));
            laserGroup.getChildren().add(laser);
            laserGroup.toFront();

            FadeTransition fade = new FadeTransition(Duration.millis(600), laser);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(event -> laserGroup.getChildren().remove(laser));
            fade.play();
        });
    }

    private int bulletRangeForKind(String kind) {
        return switch (kind == null ? "normal" : kind.toLowerCase()) {
            case "sniper" -> 10;
            case "heavy" -> 3;
            case "bomber" -> 4;
            default -> 5;
        };
    }

    private static class RobotViewState {
        final int x;
        final int y;
        final String direction;
        final String kind;

        RobotViewState(int x, int y, String direction, String kind) {
            this.x = x;
            this.y = y;
            this.direction = direction == null ? "NORTH" : direction;
            this.kind = kind == null ? "normal" : kind;
        }
    }

    private void startTerminalInput() {
        Thread terminalThread = new Thread(() -> {
            try (BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.println("Connected to robot server. Type 'launch <name>' to create your robot, or 'quit' to quit.");
                System.out.println("Enter messages to send to the server:");

                String input;
                while ((input = keyboard.readLine()) != null) {
                    if ("quit".equalsIgnoreCase(input.trim())) {
                        Platform.exit(); // Close the JavaFX window
                        System.exit(0);  // Kill the application completely
                        break;
                    }

                    try {
                        final String jsonRequest = JsonProtocol.requestFromConsoleInput(input);
                        // Send it safely using the JavaFX thread
                        Platform.runLater(() -> sendCommand(jsonRequest)); 
                    } catch (Exception e) {
                        System.err.println("Error formatting command.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Terminal input closed.");
            }
        });
        terminalThread.setDaemon(true);
        terminalThread.start();
    }

    private WorldConfig loadConfig(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            File configFile = new File(filename);
            if (configFile.exists()) {
                return mapper.readValue(configFile, WorldConfig.class);
            } else {
                System.err.println("GUI: Config file not found: " + filename + ". No obstacles will be loaded.");
            }
        } catch (IOException e) {
            System.err.println("GUI: Error reading config file: " + e.getMessage());
        }
        return null;
    }

    // --- Nested classes for config parsing ---
    public static class ObstacleConfig {
        public int x;
        public int y;
        public String type;
    }

    public static class WorldConfig {
        public int width;
        public int height;
        public int visibility;
        public List<ObstacleConfig> obstacles;
    }

    public static class RobotStateNestedDTO {
        public Integer shields;
        public Integer shots;
    }

    public static class RobotDTO {
        public String name;
        public String kind;
        public int[] position;
        public int x;
        public int y;
        public int shields = 5;
        public int shots = 5;
        public String direction;
        public RobotStateNestedDTO state;

        public int getX() {
            if (position != null && position.length > 0) return position[0];
            return x;
        }

        public int getY() {
            if (position != null && position.length > 1) return position[1];
            return y;
        }

        public int getShields() {
            if (state != null && state.shields != null) return state.shields;
            return shields;
        }

        public int getShots() {
            if (state != null && state.shots != null) return state.shots;
            return shots;
        }
    }

    public static class RobotStateNestedResponse {
        public List<RobotDTO> robots;
    }

    public static class GameStateResponse {
        public List<RobotDTO> robots;
        public RobotStateNestedResponse state;
        public com.fasterxml.jackson.databind.JsonNode data;
    }


    public static void main(String[] args) {
        // Disable Marlin's use of Unsafe to prevent terminally deprecated warnings on newer JDKs
        System.setProperty("prism.marlin.useUnsafe", "false");
        launch(args);
    }
}
