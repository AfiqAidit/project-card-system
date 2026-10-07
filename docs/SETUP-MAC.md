# Mac setup notes (Phase 0)

Tools install **for the whole Mac** via Homebrew, not inside this folder.

| Tool | Install | What it does |
|------|---------|----------------|
| Java 21 | Already on Mac: `openjdk@21` | Runs and compiles Java |
| Maven | `brew install maven` | Builds the WAR from `pom.xml` |
| WildFly | `brew install wildfly-as` | Application server (JBoss family) |

Docker is **not** required for Phase 0. Oracle Database in Docker comes later; the sprint may use H2 first.

## Paths (Apple Silicon Homebrew)

- WildFly: `/opt/homebrew/opt/wildfly-as/libexec`
- Maven: `/opt/homebrew/bin/mvn`

## Shell exports (add to `~/.zshrc` when you are ready)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export JBOSS_HOME=/opt/homebrew/opt/wildfly-as/libexec
export PATH="$PATH:$JBOSS_HOME/bin"
```

Open a **new** terminal after editing `.zshrc`.
