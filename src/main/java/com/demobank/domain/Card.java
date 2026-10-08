package com.demobank.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Base card: state changes only through authorize, freeze, and unfreeze (encapsulation).
 * Subclasses implement debit vs credit spending rules (polymorphism).
 */
public abstract class Card {

  private final UUID id;
  private final CardNumber number;
  private CardStatus status;

  protected Card(CardNumber number) {
    this.id = UUID.randomUUID();
    this.number = Objects.requireNonNull(number);
    this.status = CardStatus.ACTIVE;
  }

  public UUID id() {
    return id;
  }

  public CardNumber number() {
    return number;
  }

  public CardStatus status() {
    return status;
  }

  public void freeze() {
    status = CardStatus.FROZEN;
  }

  public void unfreeze() {
    status = CardStatus.ACTIVE;
  }

  public AuthorizationResult authorize(Money amount) {
    if (amount == null || amount.amount().signum() <= 0) {
      return AuthorizationResult.declined(DeclineReason.INVALID_AMOUNT, "Amount must be positive");
    }
    if (status == CardStatus.FROZEN) {
      return AuthorizationResult.declined(DeclineReason.CARD_FROZEN, "Card is frozen");
    }
    if (!canSpend(amount)) {
      return declineForInsufficient(amount);
    }
    applySpend(amount);
    return AuthorizationResult.approved();
  }

  protected abstract boolean canSpend(Money amount);

  protected abstract void applySpend(Money amount);

  protected abstract AuthorizationResult declineForInsufficient(Money amount);

  public abstract Money availableToSpend();
}
