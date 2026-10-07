#!/usr/bin/env bash
# Demo Bank card-system: build and deploy to local WildFly.
# WildFly must be running before deploy (see README).

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21 2>/dev/null || true)}"
export JBOSS_HOME="${JBOSS_HOME:-/opt/homebrew/opt/wildfly-as/libexec}"

if [[ -z "$JAVA_HOME" || ! -d "$JAVA_HOME" ]]; then
  echo "JAVA_HOME not set. Run: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
  exit 1
fi

if [[ ! -x "$JBOSS_HOME/bin/standalone.sh" ]]; then
  echo "WildFly not found at JBOSS_HOME=$JBOSS_HOME"
  echo "Install: brew install wildfly-as"
  exit 1
fi

wildfly_up() {
  curl -sf -o /dev/null "http://localhost:8080/" 2>/dev/null
}

usage() {
  cat <<'EOF'
Usage: ./scripts/dev.sh <command>

  package    mvn clean package (WAR only, no deploy)
  deploy     package + wildfly:deploy (WildFly must be running)
  redeploy   package + wildfly:redeploy
  undeploy   remove app from WildFly
  urls       print local URLs

Start WildFly (separate terminal):
  $JBOSS_HOME/bin/standalone.sh
  or: brew services start wildfly-as
EOF
}

cmd="${1:-}"

case "$cmd" in
  package)
    mvn clean package
    echo "WAR: $ROOT/target/card-system.war"
    ;;
  deploy)
    if ! wildfly_up; then
      echo "WildFly does not look reachable on http://localhost:8080/"
      echo "Start it first: \$JBOSS_HOME/bin/standalone.sh"
      exit 1
    fi
    mvn clean package wildfly:deploy
    echo "Open: http://localhost:8080/card-system/"
    ;;
  redeploy)
    if ! wildfly_up; then
      echo "WildFly does not look reachable on http://localhost:8080/"
      exit 1
    fi
    mvn clean package wildfly:redeploy
    echo "Redeployed: http://localhost:8080/card-system/"
    ;;
  undeploy)
    mvn wildfly:undeploy
    ;;
  urls)
    echo "http://localhost:8080/card-system/"
    echo "http://localhost:8080/card-system/hello"
    ;;
  ""|-h|--help)
    usage
    ;;
  *)
    echo "Unknown command: $cmd"
    usage
    exit 1
    ;;
esac
