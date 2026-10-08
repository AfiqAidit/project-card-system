package com.demobank.domain;

import java.util.Objects;

/** Demo cards use prefix 999 and pass the Luhn check. Only masked values should be shown in UI. */
public final class CardNumber {

  private static final String DEMO_PREFIX = "999";
  private static final int LENGTH = 16;

  private final String digits;

  private CardNumber(String digits) {
    this.digits = digits;
  }

  public static CardNumber of(String raw) {
    String digits = raw.replaceAll("\\D", "");
    if (digits.length() != LENGTH) {
      throw new IllegalArgumentException("Card number must be 16 digits");
    }
    if (!digits.startsWith(DEMO_PREFIX)) {
      throw new IllegalArgumentException("Demo cards must start with " + DEMO_PREFIX);
    }
    if (!luhnValid(digits)) {
      throw new IllegalArgumentException("Invalid card number (Luhn check failed)");
    }
    return new CardNumber(digits);
  }

  /** Generate a valid demo number: 999 + 12 digits you choose + Luhn check digit. */
  public static CardNumber generateDemo(String twelveDigitsAfterPrefix) {
    String base = DEMO_PREFIX + twelveDigitsAfterPrefix.replaceAll("\\D", "");
    if (base.length() != 15) {
      throw new IllegalArgumentException("Need 12 digits after prefix for generation");
    }
    for (int d = 0; d <= 9; d++) {
      String full = base + d;
      if (luhnValid(full)) {
        return new CardNumber(full);
      }
    }
    throw new IllegalStateException("Could not compute Luhn check digit");
  }

  public static boolean luhnValid(String digits) {
    if (!digits.matches("\\d+")) {
      return false;
    }
    int sum = 0;
    boolean doubleDigit = false;
    for (int i = digits.length() - 1; i >= 0; i--) {
      int d = digits.charAt(i) - '0';
      if (doubleDigit) {
        d *= 2;
        if (d > 9) {
          d -= 9;
        }
      }
      sum += d;
      doubleDigit = !doubleDigit;
    }
    return sum % 10 == 0;
  }

  public String masked() {
    return digits.substring(0, 4) + " •••• •••• " + digits.substring(12);
  }

  public String digitsForStorage() {
    return digits;
  }

  @Override
  public String toString() {
    return masked();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CardNumber that)) {
      return false;
    }
    return digits.equals(that.digits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digits);
  }
}
