# Demo Bank card system

A fictional bank card application built with **Java 21**, **Jakarta EE 10**, and **WildFly**. It is a learning and portfolio project inspired by card-system work at Finexus. Nothing here is real bank software.

Full plan and context: **`docs/CARD-SYSTEM-SPEC.md`** in this repo. For AI assistants, read **`AGENTS.md`** first.

## Phase 4 (current)

Phases 0 to 3 are in place: hello servlet, card classes, teller desk with a request trace, authorization EJB, and the concurrency lab.

- **Teller desk:** http://localhost:8080/card-system/teller
- **Back office:** http://localhost:8080/card-system/office/cards.xhtml (staff login `staff` / `demo`)
- **Database:** H2 file at `~/.demobank/` (see `docs/DATABASE.md`)
- Phase 0 hello servlet still at `/hello`

The back office also lists recent purchases (approved and declined). Login, the card list, freeze, credit limit, and that history are the Phase 4 slice in the app now.

## Prerequisites on your Mac

- Java **21** (Homebrew `openjdk@21`)
- Maven (`brew install maven`)
- WildFly (`brew install wildfly-as`)

Maven may default to a newer Java. Use Java 21 when building:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export JBOSS_HOME=/opt/homebrew/opt/wildfly-as/libexec
```

## Build

```bash
cd ~/Documents/Selangkah/card-system
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn clean package
```

The WAR is `target/card-system.war`.

## Run WildFly

Start the server (first start can take a minute):

```bash
export JBOSS_HOME=/opt/homebrew/opt/wildfly-as/libexec
$JBOSS_HOME/bin/standalone.sh
```

Or as a background service:

```bash
brew services start wildfly-as
```

Management console (optional): http://localhost:9990

## Deploy this app

With WildFly running:

```bash
./scripts/dev.sh deploy
```

Or manually:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export JBOSS_HOME=/opt/homebrew/opt/wildfly-as/libexec
mvn clean package wildfly:deploy
```

Open:

- http://localhost:8080/card-system/
- http://localhost:8080/card-system/hello

To remove the deployment:

```bash
mvn wildfly:undeploy
```

## Eclipse

1. File → Import → Maven → Existing Maven Projects → select this folder
2. Window → Show View → Servers → add WildFly (point `JBOSS_HOME` at `/opt/homebrew/opt/wildfly-as/libexec`)
3. Right-click the project → Run As → Run on Server

## License

Personal portfolio project. All data and names are fictional.
