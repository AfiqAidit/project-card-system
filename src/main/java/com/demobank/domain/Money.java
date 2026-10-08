package com.demobank.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Amount in a currency. Use BigDecimal, never double, for money. */
public final class Money {

  public static final String DEFAULT_CURRENCY = "MYR";

  private final BigDecimal amount;
  private final String currency;

  private Money(BigDecimal amount, String currency) {
    this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    this.currency = Objects.requireNonNull(currency);
  }

  public static Money of(BigDecimal amount) {
    return of(amount, DEFAULT_CURRENCY);
  }

  public static Money of(BigDecimal amount, String currency) {
    Objects.requireNonNull(amount);
    if (amount.scale() > 2) {
      throw new IllegalArgumentException("At most 2 decimal places");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    return new Money(amount, currency);
  }

  public static Money zero() {
    return of(BigDecimal.ZERO);
  }

  public BigDecimal amount() {
    return amount;
  }

  public String currency() {
    return currency;
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return of(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return of(amount.subtract(other.amount), currency);
  }

  public boolean isGreaterThan(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  public boolean isLessThan(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) < 0;
  }

  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException("Currency mismatch");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money money)) {
      return false;
    }
    return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount.stripTrailingZeros(), currency);
  }
}
