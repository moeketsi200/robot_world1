package za.co.wethinkcode.robots.server.world;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MazeLoader {

    private static boolean[][] wallGrid;
    private static boolean[][] obstacleGrid;
    private static int mazeWidth = 0;
    private static int mazeHeight = 0;
    private static boolean loaded = false;

    public static void loadMaze(String filename) {
        if (!loaded) {
            readAndDisplayMaze(filename);
            loaded = true;
        }
    }

    private static void readAndDisplayMaze(String filename) {
        List<List<Integer>> allNumbers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                List<Integer> numbers = new ArrayList<>();

                for (String part : parts) {
                    if (!part.isEmpty()) {
                        try {
                            int num = Integer.parseInt(part);
                            if (num != 999) {
                                numbers.add(num);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (!numbers.isEmpty()) {
                    allNumbers.add(numbers);
                }
            }

            if (allNumbers.isEmpty()) {
                throw new RuntimeException("Maze file is empty or invalid");
            }

            mazeHeight = allNumbers.size();
            mazeWidth = allNumbers.get(0).size();

            wallGrid = new boolean[mazeHeight][mazeWidth];
            obstacleGrid = new boolean[mazeHeight][mazeWidth];

            for (int row = 0; row < mazeHeight; row++) {
                List<Integer> rowNumbers = allNumbers.get(row);

                for (int col = 0; col < mazeWidth; col++) {
                    int value = rowNumbers.get(col);

                    // Wall characters (border)
                    if (value == 201 || value == 205 || value == 187 ||
                            value == 186 || value == 188 || value == 200) {
                        wallGrid[row][col] = true;
                        obstacleGrid[row][col] = false;
                    }
                    // Obstacle (solid block)
                    else if (value == 219) {
                        wallGrid[row][col] = false;
                        obstacleGrid[row][col] = true;
                    }
                    // Empty space
                    else {
                        wallGrid[row][col] = false;
                        obstacleGrid[row][col] = false;
                    }
                }
            }

            displayAsCP437(allNumbers);

        } catch (IOException e) {
            throw new RuntimeException("Error reading maze file: " + e.getMessage());
        }
    }

    private static void displayAsCP437(List<List<Integer>> allNumbers) {
        System.out.println("\n=== MAZE LOADED ===");

        for (List<Integer> numbers : allNumbers) {
            byte[] bytes = new byte[numbers.size()];
            for (int i = 0; i < numbers.size(); i++) {
                bytes[i] = numbers.get(i).byteValue();
            }
            String decoded = new String(bytes, Charset.forName("IBM437"));
            System.out.println(decoded);
        }

        System.out.println("===================\n");
    }

    public static int getMazeWidth() {
        return mazeWidth;
    }

    public static int getMazeHeight() {
        return mazeHeight;
    }

    public static boolean isWall(int x, int y) {
        if (!loaded) return true;
        if (y < 0 || y >= mazeHeight || x < 0 || x >= mazeWidth) return true;
        return wallGrid[y][x];
    }

    public static boolean isMazeObstacle(int x, int y) {
        if (!loaded) return false;
        if (y < 0 || y >= mazeHeight || x < 0 || x >= mazeWidth) return false;
        return obstacleGrid[y][x];
    }

    public static boolean isMazeLoaded() {
        return loaded;
    }
}