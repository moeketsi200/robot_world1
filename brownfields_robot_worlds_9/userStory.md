git# Robot World — User Stories

User stories are written from the perspective of the people who use the system.
Robot World has two distinct users:
- **Player** — the person running the client and controlling a robot
- **Operator** — the person running the server and managing the world
  Format: "As a [who], I want to [what], so that [why]."
  Each story includes acceptance criteria — the conditions that must be true for the story to be considered done.

---

## Epic 1: Connecting to the World
 
---

### US-001: Start the server
**As an operator, I want to start the world server from the command line, so that players can connect and play.**

**Acceptance criteria:**
- Server starts with a single command
- Port number is configurable via command line argument
- World size and maze type are configurable via command line arguments
- Server prints confirmation that it is running and on which port
- Server renders the initial world state to the console on startup
- Server does not crash if no arguments are provided — sensible defaults are used
---

### US-002: Connect to the server
**As a player, I want to connect to a running server using its IP address and port, so that I can enter the world.**

**Acceptance criteria:**
- Client accepts IP address and port as command line arguments
- Client prints a clear usage message if arguments are missing or invalid
- Client prints a clear error if the server is unreachable
- On successful connection, player sees a welcome message
- Connection works from a different machine on the same network, not just localhost
---

### US-003: Launch a robot
**As a player, I want to launch my robot into the world with a name and make, so that I can start playing.**

**Acceptance criteria:**
- Player types `launch <make> <name>` to enter the world
- Robot is placed at a random free position facing NORTH
- Player receives confirmation with their robot's starting position and the world's visibility range
- If the robot name is already taken, player receives a clear error message
- If the world is full, player receives a clear error message
- A player cannot launch more than one robot on the same connection
- A second `launch` attempt on the same connection is rejected with a clear error
---

### US-004: Connect from another machine
**As a player, I want to connect to a server running on a different machine, so that I can play against other teams.**

**Acceptance criteria:**
- Client connects using the server machine's actual IP address
- Server binds to all network interfaces, not just localhost
- Connection works across machines on the same local network
- Required for the Robot Wars tournament
---

## Epic 2: Exploring the World
 
---

### US-005: Look around
**As a player, I want to look around in all four directions, so that I can understand my surroundings before deciding what to do.**

**Acceptance criteria:**
- Player types `look` to scan in all four directions
- Response lists what is visible in each direction: obstacles, robots, edges
- Each sighting includes the direction and distance in steps
- Directions are reported as absolute compass directions (NORTH, SOUTH, EAST, WEST)
- Results are displayed in a readable format, not raw JSON
- Robots can see through lakes and pits but not through mountains (iteration 3)
---

### US-006: Check robot state
**As a player, I want to check my robot's current state, so that I know my position, direction, shields and ammunition.**

**Acceptance criteria:**
- Player types `state` to get current robot information
- Response shows: position, direction, shield strength, shots remaining, operational status
- Response is displayed in a readable format (not raw JSON)
- State reflects the most recent actions taken
---

### US-007: Check orientation
**As a player, I want to check which direction my robot is facing, so that I can plan my next move.**

**Acceptance criteria:**
- Player types `orientation` to get current facing direction
- Response reports direction as one of: north, south, east, west
- Response updates correctly after turning
---

### US-008: Visibility constraint
**As a player, I want the world to limit how far I can see, so that the game has a strategic element of limited information.**

**Acceptance criteria:**
- Robot cannot see beyond the world's configured visibility range
- Objects further than the visibility range do not appear in `look` results
- Visibility range is communicated to the player when they launch
- Visibility is the same for all robots and configured server-side
---

## Epic 3: Moving Around
 
---

### US-009: Move forward
**As a player, I want to move my robot forward, so that I can navigate the world.**

**Acceptance criteria:**
- Player types `forward <steps>` to move in the direction the robot is facing
- Robot moves the specified number of steps if unobstructed
- If an obstacle or edge is encountered, robot stops at the last valid position
- If another robot is in the path, movement is blocked
- Response confirms how many steps were actually taken
- World renders updated position on the server console
---

### US-010: Move backward
**As a player, I want to move my robot backward, so that I can retreat without turning around.**

**Acceptance criteria:**
- Player types `back <steps>` to move in the opposite direction to facing
- Same obstruction rules apply as forward movement
- Response confirms how many steps were taken
- Robot direction does not change when moving backward
---

### US-011: Turn the robot
**As a player, I want to turn my robot left or right, so that I can change the direction I'm facing.**

**Acceptance criteria:**
- Player types `turn left` or `turn right`
- Robot rotates 90 degrees in the specified direction
- Subsequent `forward` and `back` commands use the new direction
- `state` and `orientation` reflect the updated direction
- Turning does not change the robot's position
---

### US-012: Honour world boundaries
**As a player, I want movement to stop at the world's edges, so that my robot cannot leave the world.**

**Acceptance criteria:**
- Robot cannot move beyond the world boundary
- Movement stops at the last valid position before the edge
- Edge is reported as an obstacle in `look` results
- No error or crash occurs when a robot reaches the boundary
---

### US-013: Honour obstacles
**As a player, I want movement to be blocked by obstacles, so that the world has strategic terrain.**

**Acceptance criteria:**
- Robot cannot move through mountains or lakes
- Movement stops at the last valid position before the obstacle
- A robot that moves into a bottomless pit is killed and removed from the world
- Robots can see over lakes and pits but not through mountains
---

## Epic 4: Combat
 
---

### US-014: Fire weapon
**As a player, I want to fire my robot's gun, so that I can attack other robots.**

**Acceptance criteria:**
- Player types `fire` to shoot in the direction the robot is facing
- Shot travels the configured distance for the robot's weapon
- If another robot is in the line of fire within range, it takes one hit
- Shooting an obstacle has no effect
- A robot can shoot another it cannot see (e.g. through fog)
- Response indicates hit or miss, and if hit, the name and distance of the target
- Number of shots remaining is updated after firing
- Robot cannot fire if it has no shots remaining
---

### US-015: Take damage
**As a player, I want my robot's shields to absorb hits, so that I have a chance to survive being shot.**

**Acceptance criteria:**
- Each hit reduces shield strength by one
- Shield strength is visible in `state` response
- When shields reach zero, the next hit kills the robot
- Killed robot is removed from the world
- Player whose robot was killed is informed
- Maximum shield strength is configured by the world
---

### US-016: Repair shields
**As a player, I want to repair my robot's shields, so that I can survive longer in combat.**

**Acceptance criteria:**
- Player types `repair` to begin repairing
- Repair takes the world-configured number of seconds
- Robot cannot move while being repaired
- Shields are fully restored to configured maximum when repair completes
- Partial repair is not possible — it is all or nothing
- `state` shows status as `REPAIR` during the process
---

### US-017: Reload weapon
**As a player, I want to reload my robot's weapon, so that I can continue fighting after running out of shots.**

**Acceptance criteria:**
- Player types `reload` to begin reloading
- Reload takes the world-configured number of seconds
- Robot cannot move while reloading
- Weapon is fully reloaded to maximum shots when complete
- Partial reload is not possible — it is all or nothing
- `state` shows status as `RELOAD` during the process
- Maximum shots is determined by the weapon's configured distance
---

## Epic 5: World Management (Operator)
 
---

### US-018: List all robots
**As an operator, I want to see all robots currently in the world, so that I can monitor the game.**

**Acceptance criteria:**
- Operator types `robots` in the server console
- Response lists every robot with: name, make, position, direction, shields, shots, status
- Shows total count of robots
- Shows "(no robots connected)" when world is empty
---

### US-019: Dump world state
**As an operator, I want to see a full snapshot of the world, so that I can diagnose issues or monitor gameplay.**

**Acceptance criteria:**
- Operator types `dump` in the server console
- Response shows: world dimensions, visibility setting, all obstacle positions, all robot states
- Output is clearly formatted and readable
---

### US-020: Shut down the server
**As an operator, I want to shut down the server cleanly, so that all connected robots are disconnected gracefully.**

**Acceptance criteria:**
- Operator types `quit` in the server console
- All connected clients are notified and disconnected
- All robots are removed from the world
- Server process exits cleanly

---

## Epic 6: Reliable Delivery and CI/CD

### US-021: Automate the project delivery pipeline
**As a project maintainer, I want the project to automatically build, test, package, and validate the server whenever changes are made, so that the whole project can be delivered reliably and consistently.**

**Acceptance criteria:**
- The pipeline runs automatically on every push or merge to the main branch
- The pipeline compiles the project successfully
- The pipeline runs all unit tests successfully
- The pipeline packages the server into a distributable JAR
- The pipeline starts the server and runs acceptance tests against it
- The pipeline stops the server cleanly after acceptance tests complete
- A failed stage produces a clear failure so the team can fix issues quickly
- The built artifact is available for release or deployment
---

### US-021: Configure the world
**As an operator, I want to configure world size, visibility, shield repair time, reload time and maximum shield strength, so that I can control the game rules.**

**Acceptance criteria:**
- World size configurable via command line or config file
- Visibility range configurable
- Shield repair time configurable in seconds
- Weapon reload time configurable in seconds
- Maximum shield strength configurable in hits
- If no config provided, sensible defaults are used
- Configuration is read on startup — changes require restart
---

## Epic 6: Different Robot Makes
 
---

### US-022: Choose a robot make
**As a player, I want to choose a make (model) of robot with different capabilities, so that I can pick a playstyle that suits me.**

**Acceptance criteria:**
- Player specifies make when launching: `launch <make> <name>`
- Different makes have different shield strength and weapon configurations
- For example: a Sniper robot has long range (5 steps) but only 1 shield
- A Tank robot has maximum shields but short range
- Make is stored and visible in robot state
---

### US-023: Weapon distance affects shot count
**As a player, I want to understand the tradeoff between weapon range and ammunition, so that I can choose a robot make strategically.**

**Acceptance criteria:**
- Longer range weapons carry fewer shots
- Shorter range weapons carry more shots
- The relationship follows the spec table: distance 5 = 1 shot, distance 1 = 5 shots, distance 0 = no weapon
- Shot count and distance visible when robot is launched and in `state`
---

## Epic 7: Robot Wars Tournament
 
---

### US-024: Host a home match
**As a team, I want to host our server and invite another team to connect their robots, so that we can compete on our home world.**

**Acceptance criteria:**
- Our server runs on our machine and is reachable from another team's machines
- Other team's robots can connect and are subject to our world's rules
- Score is tracked as number of kills
---

### US-025: Play an away match
**As a team, I want to connect our robots to another team's server, so that we can compete on their home world.**

**Acceptance criteria:**
- Our client connects to another team's server using their IP and port
- Our robots obey their world's configuration
- Protocol is compatible — any compliant client connects to any compliant server
---

### US-026: Determine match winner
**As a tournament participant, I want a clear scoring system, so that there is a fair winner.**

**Acceptance criteria:**
- Score = number of kills in a match
- Home and away matches both count
- If scores tied after home and away, team with more away kills wins
- If still tied, sudden death match: one robot per team, coin flip for world, last robot standing wins
- Dead robots cannot re-launch in the same match
---