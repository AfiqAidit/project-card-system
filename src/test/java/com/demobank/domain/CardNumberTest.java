package com.demobank.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CardNumberTest {

  @Test
  void generateDemo_passesLuhn() {
    CardNumber n = CardNumber.generateDemo("123456789012");
    assertTrue(CardNumber.luhnValid(n.digitsForStorage()));
    assertTrue(n.digitsForStorage().startsWith("999"));
  }

  @Test
  void masked_hidesMiddleDigits() {
    CardNumber n = CardNumber.generateDemo("000000000001");
    String m = n.masked();
    assertTrue(m.contains("••••"));
    assertFalse(m.contains(n.digitsForStorage().substring(4, 12)));
  }

  @Test
  void rejectsWrongPrefix() {
    assertThrows(IllegalArgumentException.class, () -> CardNumber.of("4111111111111111"));
  }

  @Test
  void rejectsFailedLuhn() {
    assertThrows(IllegalArgumentException.class, () -> CardNumber.of("9999123456789012"));
  }
}
