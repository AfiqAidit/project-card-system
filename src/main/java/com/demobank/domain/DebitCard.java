package com.demobank.domain;

import java.math.BigDecimal;

public final class DebitCard extends Card {

  private Money balance;

  public DebitCard(CardNumber number, Money openingBalance) {
    super(number);
    this.balance = openingBalance;
  }

  @Override
  protected boolean canSpend(Money amount) {
    return !balance.isLessThan(amount);
  }

  @Override
  protected void applySpend(Money amount) {
    balance = balance.subtract(amount);
  }

  @Override
  protected AuthorizationResult declineForInsufficient(Money amount) {
    return AuthorizationResult.declined(
        DeclineReason.INSUFFICIENT_FUNDS, "Insufficient balance for " + amount.amount());
  }

  @Override
  public Money availableToSpend() {
    return balance;
  }

  public Money balance() {
    return balance;
  }
}
