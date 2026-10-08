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

## WildFly must use Java 21 (not JDK 27+)

Homebrew may default to a newer JDK. If WildFly starts on JDK 27, JPA deploy can fail with ByteBuddy errors and `/teller` stays 404 while an old WAR still serves `/hello`.

Homebrew's `standalone.sh` **hardcodes** `/opt/homebrew/opt/openjdk/bin/java` (often JDK 27), so `JAVA_HOME` alone is not enough. Use the project script:

```bash
./scripts/start-wildfly.sh
```

That sets `JAVA` and `JAVA_HOME` to Java 21, then runs WildFly.

If WildFly was already running on the wrong JDK, stop it (Ctrl+C in that terminal, or `brew services stop wildfly-as`), then run `./scripts/start-wildfly.sh` again.

Check the running server log: `grep java.version $JBOSS_HOME/standalone/log/server.log | tail -1` should show `21`, not `27`.

## Dev script

From the project root:

```bash
./scripts/dev.sh package   # build WAR only
./scripts/dev.sh deploy    # build + deploy (WildFly must be running)
./scripts/dev.sh redeploy  # after code changes
./scripts/dev.sh undeploy  # remove app, WildFly keeps running
./scripts/dev.sh urls
```

`deploy` runs `mvn clean package` then `wildfly:deploy`. It does not start WildFly.
