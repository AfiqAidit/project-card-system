package com.demobank.web;

import java.math.BigDecimal;

public record ConcurrencyRunResult(
    String mode,
    int attempts,
    int approved,
    int declined,
    BigDecimal balanceBefore,
    BigDecimal balanceAfter,
    BigDecimal amountEach,
    /** Max purchases that could succeed if done one after another (balance ÷ amount). */
    int maxFairApprovals) {}
