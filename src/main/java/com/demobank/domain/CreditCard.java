package com.demobank.domain;

import java.util.UUID;

public final class CreditCard extends Card {

  private Money creditLimit;
  private Money amountUsed;

  public CreditCard(CardNumber number, Money creditLimit) {
    super(number);
    this.creditLimit = creditLimit;
    this.amountUsed = Money.zero();
  }

  public CreditCard(CardNumber number, Money creditLimit, UUID existingId) {
    super(number, existingId);
    this.creditLimit = creditLimit;
    this.amountUsed = Money.zero();
  }

  public void restoreState(CardStatus status, Money used) {
    restoreStatus(status);
    this.amountUsed = used;
  }

  /** Staff can raise or lower the limit. It cannot drop below what is already spent. */
  public void changeLimit(Money newLimit) {
    if (amountUsed.isGreaterThan(newLimit)) {
      throw new IllegalArgumentException("Limit cannot be below the amount already used");
    }
    this.creditLimit = newLimit;
  }

  @Override
  protected boolean canSpend(Money amount) {
    Money after = amountUsed.add(amount);
    return !after.isGreaterThan(creditLimit);
  }

  @Override
  protected void applySpend(Money amount) {
    amountUsed = amountUsed.add(amount);
  }

  @Override
  protected AuthorizationResult declineForInsufficient(Money amount) {
    return AuthorizationResult.declined(
        DeclineReason.OVER_CREDIT_LIMIT, "Over credit limit for " + amount.amount());
  }

  @Override
  public Money availableToSpend() {
    return creditLimit.subtract(amountUsed);
  }

  public Money creditLimit() {
    return creditLimit;
  }

  public Money amountUsed() {
    return amountUsed;
  }
}
