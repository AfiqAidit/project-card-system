# Domain design (OOP)

Fictional Demo Bank cards only. Prefix **999**, Luhn-valid numbers.

## Encapsulation

`Card` has no public setters for balance or status. Spending goes through `authorize(Money)`. Freeze via `freeze()` / `unfreeze()`. A credit card changes its limit only through `changeLimit(Money)`, and the new limit cannot sit below the amount already used.

## Inheritance and polymorphism

- `DebitCard`: spends from `balance`.
- `CreditCard`: spends against `creditLimit` and tracks `amountUsed`.

`authorize` calls `canSpend` and `applySpend` on the subclass without `if (debit)`.

## Value objects

- `Money`: `BigDecimal`, currency (default MYR).
- `CardNumber`: validation, masking for display.

JPA entities and database persistence come in Phase 2 (teller screen).

## Web UI (all pages)

Use shared **Demo Bank** chrome from `css/demobank.css`: `db-shell`, `db-topbar`, `db-nav`, `panel`, `db-form`, `db-flash`. The same nav (Home, Teller, Back office, Lab) sits on every page.

`css/teller.css` is only the request trace, the teller tour, and the side-by-side trace layout. Do not put `body`, `.panel`, or `button` resets there. That file is linked second, so those rules override the shared chrome and squeeze the page.
