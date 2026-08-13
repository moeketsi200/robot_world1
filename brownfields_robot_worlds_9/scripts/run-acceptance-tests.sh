#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

cleanup() {
  if [[ -n "${SERVER_PID:-}" ]]; then
    kill "$SERVER_PID" 2>/dev/null || true
    wait "$SERVER_PID" 2>/dev/null || true
    SERVER_PID=""
  fi
}
trap cleanup EXIT

start_server() {
  local args="$1"
  mvn -q exec:java -Dexec.mainClass=za.co.wethinkcode.robots.server.Server -Dexec.args="$args" > /tmp/robot-world-server.log 2>&1 &
  SERVER_PID=$!

  for _ in $(seq 1 20); do
    if python3 - <<'PY' >/dev/null 2>&1
import socket
import sys

sock = socket.socket()
sock.settimeout(1)
try:
    sock.connect(("127.0.0.1", 5000))
except OSError:
    sys.exit(1)
finally:
    sock.close()
PY
    then
      return 0
    fi
    sleep 1
  done
  echo "Server failed to start with args: $args"
  cat /tmp/robot-world-server.log
  return 1
}

mvn -q -DskipTests compile

# 1. Run tests for a 2x2 world without obstacles
start_server "-p 5000 -s 2 -o none"
mvn -q test -Dtest='za.co.wethinkcode.robots.acceptencetest.LaunchRobotTests#validLaunchShouldSucceed+canLaunchAnotherRobot+invalidLaunchShouldFail+noMoreSpaceInTheWorldForAnotherRobot+robotWithSameNameAlreadyInTheWorld+worldWithoutObstaclesIsFull,za.co.wethinkcode.robots.acceptencetest.LookRobotTests,za.co.wethinkcode.robots.acceptencetest.PurgeCommandTest'
cleanup

# 2. Run tests for a 2x2 world with an obstacle at [1,1]
start_server "-p 5000 -s 2 -o 1,1"
mvn -q test -Dtest='za.co.wethinkcode.robots.acceptencetest.LaunchRobotTests#launchRobotsIntoWorldWithObstacle+worldWithAnObstacleIsFull'
cleanup
