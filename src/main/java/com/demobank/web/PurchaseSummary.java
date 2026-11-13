package com.demobank.web;

import java.math.BigDecimal;

/** One row on the back office purchase list. The card number is already masked. */
public record PurchaseSummary(
    String when,
    String maskedNumber,
    BigDecimal amount,
    boolean approved,
    String reason) {}
