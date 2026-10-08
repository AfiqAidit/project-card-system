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
