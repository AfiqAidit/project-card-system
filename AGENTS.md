# AI handoff for this repo

Read these first, in order:

1. **`.cursor/rules/continue-card-system.mdc`** - where the build stopped (13 Nov 2026), what is uncommitted, and what to do next. This wins if it disagrees with the spec.
2. **`docs/CARD-SYSTEM-SPEC.md`** - full product context, job target, glossary, phases, "Show what's happening" design, hard rules
3. **`README.md`** - build, run WildFly, deploy
4. **`docs/SETUP-MAC.md`** - Mac paths and `JAVA_HOME` / `JBOSS_HOME`

## Owner

Muhammad Afiq Aidit. New to Java EE; knows Java 21 and Spring Boot. Explain in plain English, one step at a time, compare to Laravel or Spring where helpful.

## Rules

- NDA safe: fictional Demo Bank only, prefix `999` for card numbers
- No em dashes or en dashes in any text
- Java **21** for compile: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` before `mvn`
- Commits: short message, owner only, no `Co-authored-by` trailers

## Portfolio

The Next.js site is a **separate repo** at `~/Documents/Selangkah/Portfolio`. Link this project from `side-projects.ts` when ready; do not merge repos.
