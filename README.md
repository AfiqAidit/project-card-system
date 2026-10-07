# Demo Bank card system

A fictional bank card application built with **Java 21**, **Jakarta EE 10**, and **WildFly**. It is a learning and portfolio project inspired by card-system work at Finexus. Nothing here is real bank software.

Full plan and context: see `CARD-SYSTEM-SPEC.md` in the portfolio repo (`Portfolio/docs/CARD-SYSTEM-SPEC.md`).

## Phase 0 (current)

- Maven builds a `.war` file
- One servlet at `/hello` and a JSP home page

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

With WildFly running, from the project folder:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export JBOSS_HOME=/opt/homebrew/opt/wildfly-as/libexec
mvn wildfly:deploy
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
