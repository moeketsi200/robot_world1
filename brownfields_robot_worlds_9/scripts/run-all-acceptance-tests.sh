#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

cleanup() {
  pkill -9 -f 'reference-server' 2>/dev/null || fuser -k -9 5000/tcp 2>/dev/null || true
  sleep 2
}
trap cleanup EXIT

echo "=== 1/3: Running 1x1 Acceptance Tests ==="
cleanup
java -jar libs/reference-server-0.2.3.jar &
sleep 2
mvn test -Dtest='!LaunchRobotTests#launchRobotsIntoWorldWithObstacle,!LaunchRobotTests#worldWithAnObstacleIsFull,!LaunchRobotTests#canLaunchAnotherRobot,!LaunchRobotTests#worldWithoutObstaclesIsFull,za.co.wethinkcode.robots.acceptencetest.**'

echo "=== 2/3: Running 2x2 No-Obstacle Acceptance Tests ==="
cleanup
java -jar libs/reference-server-0.2.3.jar -s 2 &
sleep 2
mvn test -Dtest='LaunchRobotTests#canLaunchAnotherRobot,LaunchRobotTests#worldWithoutObstaclesIsFull'

echo "=== 3/3: Running 2x2 Obstacle Acceptance Test ==="
cleanup
java -jar libs/reference-server-0.2.3.jar -s 2 -o 1,1 &
sleep 2
mvn test -Dtest='LaunchRobotTests#launchRobotsIntoWorldWithObstacle,LaunchRobotTests#worldWithAnObstacleIsFull'

echo "=== All Acceptance Tests Completed Successfully! ==="
