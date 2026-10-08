package com.demobank.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CardAuthorizationTest {

  private static final CardNumber NUMBER = CardNumber.generateDemo("111111111111");

  @Test
  void debit_approvesWhenBalanceEnough() {
    DebitCard card = new DebitCard(NUMBER, Money.of(new BigDecimal("100.00")));
    AuthorizationResult r = card.authorize(Money.of(new BigDecimal("40.00")));
    assertTrue(r.success());
    assertEquals(new BigDecimal("60.00"), card.balance().amount());
  }

  @Test
  void debit_declinesWhenInsufficient() {
    DebitCard card = new DebitCard(NUMBER, Money.of(new BigDecimal("10.00")));
    AuthorizationResult r = card.authorize(Money.of(new BigDecimal("10.01")));
    assertFalse(r.success());
    assertEquals(DeclineReason.INSUFFICIENT_FUNDS, r.declineReason());
    assertEquals(new BigDecimal("10.00"), card.balance().amount());
  }

  @Test
  void credit_respectsLimit() {
    CreditCard card = new CreditCard(NUMBER, Money.of(new BigDecimal("500.00")));
    assertTrue(card.authorize(Money.of(new BigDecimal("200.00"))).success());
    AuthorizationResult r = card.authorize(Money.of(new BigDecimal("301.00")));
    assertFalse(r.success());
    assertEquals(DeclineReason.OVER_CREDIT_LIMIT, r.declineReason());
  }

  @Test
  void frozenCard_declines() {
    DebitCard card = new DebitCard(NUMBER, Money.of(new BigDecimal("100.00")));
    card.freeze();
    AuthorizationResult r = card.authorize(Money.of(new BigDecimal("1.00")));
    assertFalse(r.success());
    assertEquals(DeclineReason.CARD_FROZEN, r.declineReason());
  }
}
