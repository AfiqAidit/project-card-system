# Domain design (OOP)

Fictional Demo Bank cards only. Prefix **999**, Luhn-valid numbers.

## Encapsulation

`Card` has no public setters for balance or status. Spending goes through `authorize(Money)`. Freeze via `freeze()` / `unfreeze()`.

## Inheritance and polymorphism

- `DebitCard`: spends from `balance`.
- `CreditCard`: spends against `creditLimit` and tracks `amountUsed`.

`authorize` calls `canSpend` and `applySpend` on the subclass without `if (debit)`.

## Value objects

- `Money`: `BigDecimal`, currency (default MYR).
- `CardNumber`: validation, masking for display.

JPA entities and database persistence come in Phase 2 (teller screen).

## Web UI (all pages)

Use shared **Demo Bank** chrome: `css/demobank.css` plus `css/teller.css` for layout/panels. Top bar: `db-kicker`, `db-topbar`, `db-topbar-links`, `panel`, `db-form`, `db-flash`. New JSP screens should match teller and concurrency lab, not one-off styles.
