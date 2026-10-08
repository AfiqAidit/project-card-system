package com.demobank.domain;

public final class CreditCard extends Card {

  private final Money creditLimit;
  private Money amountUsed;

  public CreditCard(CardNumber number, Money creditLimit) {
    super(number);
    this.creditLimit = creditLimit;
    this.amountUsed = Money.zero();
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
