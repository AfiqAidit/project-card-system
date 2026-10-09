# Database setup (Phase 2)

## What we use: H2 on your Mac (local files)

**H2** is a small SQL database (like SQLite for Java). It is **not** Oracle yet. We use it so Phase 2 works without Docker or WildFly datasource setup.

| Question | Answer |
|----------|--------|
| Is it local? | **Yes.** Data is stored on **your Mac**, not in the cloud. |
| Where is the file? | `~/.demobank/demobank.mv.db` (in your home folder) |
| Who creates tables? | **Hibernate** reads `CardEntity` and runs `hibernate.hbm2ddl.auto=update` on first use. |

## How `CardEntity` connects (no Eclipse “DB link” needed)

```text
teller.jsp  →  TellerServlet  →  CardService (EJB)
                                      │
                                      ▼
                               EntityManagerFactory ("DemoBankPU")
                                      │
                                      ▼
                               persistence.xml (JDBC URL → H2 file)
                                      │
                                      ▼
                               ~/.demobank/   table "cards"  ↔  CardEntity
```

You do **not** configure a connection in Eclipse for this to work. The URL is in `src/main/resources/META-INF/persistence.xml`.

## Deploy note

We **removed** `demobank-ds.xml` because WildFly failed to register the JDBC driver from the WAR (`jboss.jdbc-driver.demobank-h2`). The app now uses **RESOURCE_LOCAL** JPA with the H2 jar inside the WAR. That is simpler for learning; we can move to a WildFly **JTA + Oracle** datasource in a later phase.

## Schema changes (e.g. new `version` column)

If you see **Column "VERSION" not found** after a code update:

1. `./scripts/reset-demobank-db.sh` then `./scripts/dev.sh redeploy` (cleanest), or
2. Redeploy only — the app tries to add `version` on startup and before each DB call.

## Try it

1. Start WildFly.
2. `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` and `export JBOSS_HOME=...`
3. `./scripts/dev.sh redeploy`
4. Open http://localhost:8080/card-system/teller

If deploy still fails, run `mvn wildfly:undeploy` then `./scripts/dev.sh deploy`.

## Oracle later

Same `CardEntity`, new JDBC URL and driver in `persistence.xml` or a WildFly datasource.
