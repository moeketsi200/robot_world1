# Robot World

A multi-player, client-server robot simulation built in Java as part of the WeThinkCode_ curriculum.

Robots operate on a bounded 2D grid centred at `(0,0)`. Each robot is controlled by a dedicated **client** program that connects to a central **server**. The server is the single source of truth — it holds all world state, validates all commands, and enforces all game rules. Multiple clients can connect and play simultaneously.

---

## Project Structure

```
src/
└── main/java/za/co/wethinkcode/robots/
    ├── server/
    │   ├── Server.java             # Entry point — parses CLI args, accepts connections, spawns threads
    │   ├── ClientHandler.java      # Handles one connected client (implements Runnable)
    │   ├── config/
    │   │   ├── ServerArgs.java     # CLI argument parser (-p, -s, -o flags)
    │   │   ├── WorldConfig.java    # World configuration (size, port, obstacles)
    │   │   └── ConfigLoader.java   # Reads config.json on startup
    │   ├── world/
    │   │   └── World.java          # Game world state (grid, robots, obstacles)
    │   ├── commands/
    │   │   ├── Command.java        # Interface: validate() + execute()
    │   │   ├── LaunchCommand.java  # Launch robot into world
    │   │   ├── ForwardCommand.java # Move forward N steps
    │   │   ├── BackCommand.java    # Move backward N steps
    │   │   ├── TurnCommand.java    # Turn left or right
    │   │   ├── LookCommand.java    # Look in all 4 directions
    │   │   ├── StateCommand.java   # Report robot state
    │   │   ├── FireCommand.java    # Fire gun
    │   │   ├── RepairCommand.java  # Repair shields
    │   │   ├── ReloadCommand.java  # Reload weapon
    │   │   └── server/
    │   │       ├── ServerQuitCommand.java   # Shut down the server
    │   │       ├── ServerRobotsCommand.java # List robots in the world
    │   │       └── ServerDumpCommand.java   # Dump full world state
    ├── client/
    │   └── Client.java             # Client entry point
    └── api/
        └── API.java
```

---

## Prerequisites

- Java 21
- Maven 3.x
- Python 3 (used by acceptance test scripts for server health checks)

---

## CLI Arguments

The server accepts the following command-line arguments:

| Flag | Description | Default |
|------|-------------|---------|
| `-p <port>` | Port number to listen on | `5000` |
| `-s <size>` | Half-size of the world grid (world spans `-size` to `+size`) | `1` |
| `-o <x,y>` | Place a single obstacle at coordinate `[x, y]`. Use `none` for no obstacle. | `none` |

### Running with Maven (development)

Pass arguments via `-Dexec.args`:

```bash
# Default 1x1 world on port 5000
mvn exec:java -Dexec.mainClass=za.co.wethinkcode.robots.server.Server

# 2x2 world (no obstacles)
mvn exec:java -Dexec.mainClass=za.co.wethinkcode.robots.server.Server -Dexec.args="-s 2"

# 2x2 world with an obstacle at position [1,1]
mvn exec:java -Dexec.mainClass=za.co.wethinkcode.robots.server.Server -Dexec.args="-s 2 -o 1,1"

# Custom port, 5x5 world, obstacle at [3,-2]
mvn exec:java -Dexec.mainClass=za.co.wethinkcode.robots.server.Server -Dexec.args="-p 8080 -s 5 -o 3,-2"
```

### Running with the packaged JAR (production)

First build the fat JAR:

```bash
mvn -DskipTests package
```

Then run it directly — arguments go after the JAR name:

```bash
# Default 1x1 world on port 5000
java -jar target/robot-world-*.jar

# 2x2 world (no obstacles)
java -jar target/robot-world-*.jar -s 2

# 2x2 world with an obstacle at [1,1]
java -jar target/robot-world-*.jar -s 2 -o 1,1

# Custom port, 5x5 world, obstacle at [3,-2]
java -jar target/robot-world-*.jar -p 8080 -s 5 -o 3,-2
```

---

## Build, Test & Run

Make sure you are in the root directory of the project.

**Compile:**
```bash
mvn compile
```

**Run unit tests:**
```bash
mvn test
```

**Package (JAR):**
```bash
mvn -DskipTests package
```

---

## Acceptance Tests

Acceptance tests connect to a live Robot Worlds server on port `5000`. The test suite covers multiple world configurations, so the scripts manage server startup automatically.

### Option 1: Against your own server (used by CI pipeline)

```bash
./scripts/run-acceptance-tests.sh
```

This script:
1. Compiles the project
2. Starts your server with `-s 2` (2×2 world, no obstacle) and runs the standard launch tests
3. Restarts your server with `-s 2 -o 1,1` (2×2 world, obstacle at `[1,1]`) and runs the obstacle tests

### Option 2: Against the reference server (full validation)

```bash
./scripts/run-all-acceptance-tests.sh
```

Runs in three stages:

| Stage | Server Args | Tests |
|-------|-------------|-------|
| 1 | *(default 1x1)* | `validLaunchShouldSucceed`, `invalidLaunchShouldFail`, `noMoreSpaceInTheWorldForAnotherRobot`, `robotWithSameNameAlreadyInTheWorld`, and all other acceptance tests |
| 2 | `-s 2` | `canLaunchAnotherRobot`, `worldWithoutObstaclesIsFull` |
| 3 | `-s 2 -o 1,1` | `launchRobotsIntoWorldWithObstacle`, `worldWithAnObstacleIsFull` |

### Option 3: Manual

1. **Kill any existing server on port 5000:**
```bash
pkill -f 'reference-server' 2>/dev/null || fuser -k 5000/tcp 2>/dev/null || true
```

2. **Start a reference server with desired config:**
```bash
java -jar libs/reference-server-0.2.3.jar -s 2 -o 1,1 &
sleep 2
```

3. **Run specific tests:**
```bash
mvn test -Dtest='LaunchRobotTests#launchRobotsIntoWorldWithObstacle'
```

4. **Stop the server:**
```bash
pkill -f 'reference-server' || fuser -k 5000/tcp
```

---

## Run the Client

```bash
mvn exec:java -Dexec.mainClass=za.co.wethinkcode.robots.client.Client
```

---

## CI/CD Pipeline

The GitLab CI pipeline (`.gitlab-ci.yml`) runs four stages automatically on every push:

| Stage | Job | Command |
|-------|-----|---------|
| `compile` | `compile_job` | `mvn -DskipTests compile` |
| `unit-test` | `unit_test_job` | `mvn test` |
| `package` | `package_job` | `mvn -DskipTests package` |
| `acceptance-test` | `acceptance_test_job` | `./scripts/run-acceptance-tests.sh` |

---

## World Coordinate System

The centre of the world is always `(0, 0)`.

- **North** = +y (top edge = max y)
- **South** = −y (bottom edge = min y)
- **East** = +x (right edge = max x)
- **West** = −x (left edge = min x)

Distance is measured in **kliks** (one grid square).

---

## Communication Protocol

Client and server communicate via JSON over TCP sockets. Every request receives exactly one response.

**Request:**
```json
{
  "robot": "HAL",
  "command": "launch",
  "arguments": ["Sniper", "5", "1"]
}
```

**Response (success):**
```json
{
  "result": "OK",
  "data": {
    "position": [0, 0],
    "visibility": 5,
    "reload": 3,
    "repair": 3,
    "shields": 5
  },
  "state": {
    "position": [0, 0],
    "direction": "NORTH",
    "shields": 5,
    "shots": 1,
    "status": "NORMAL"
  }
}
```

**Response (error):**
```json
{
  "result": "ERROR",
  "data": { "message": "Too many of you in this world" }
}
```

Robot status values: `NORMAL`, `RELOAD`, `REPAIR`, `DEAD`

Full protocol details are in the [Architecture wiki](../../wikis/architecture).

---

## Documentation

Full design documentation lives in the [project wiki](../../wikis/home):

- [Architecture Overview](../../wikis/architecture) — coordinate system, protocol, package structure
- [Server Design](../../wikis/server-design) — all classes and their responsibilities
- [Design Decisions](../../wikis/design-decisions) — the reasoning behind key design choices
- [Iteration Log](../../wikis/iteration-log) — retrospectives and showcase outcomes

---

## Contributing

1. Pick up an issue from the [issue board](../../issues)
2. Create a branch: `git checkout -b feature/your-issue-name`
3. Write your code and tests
4. Open a merge request targeting `main`
5. At least one team member must review before merging

> All merge requests must include passing unit tests for any new behaviour.

---

## Current Status

Story: Launch Robot — complete. ✅

All acceptance tests passing:
- `validLaunchShouldSucceed`
- `invalidLaunchShouldFail`
- `noMoreSpaceInTheWorldForAnotherRobot`
- `robotWithSameNameAlreadyInTheWorld`
- `canLaunchAnotherRobot`
- `worldWithoutObstaclesIsFull`
- `launchRobotsIntoWorldWithObstacle`
- `worldWithAnObstacleIsFull`

See the [issue board](../../issues) for current task assignments.
