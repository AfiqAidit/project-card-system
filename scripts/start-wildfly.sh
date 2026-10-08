#!/usr/bin/env bash
# Start WildFly on Java 21. Homebrew's standalone.sh ignores JAVA_HOME and points at JDK 27.
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
export JBOSS_HOME="${JBOSS_HOME:-/opt/homebrew/opt/wildfly-as/libexec}"
export JAVA="${JAVA:-$JAVA_HOME/bin/java}"

if [[ ! -x "$JAVA" ]]; then
  echo "Java 21 not found. Install: brew install openjdk@21"
  exit 1
fi

echo "Using JAVA=$JAVA"
exec "$JBOSS_HOME/bin/standalone.sh" "$@"
