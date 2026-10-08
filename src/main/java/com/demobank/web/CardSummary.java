package com.demobank.web;

import java.math.BigDecimal;
import java.util.UUID;

public record CardSummary(UUID id, String maskedNumber, String kind, String status, BigDecimal available) {}
