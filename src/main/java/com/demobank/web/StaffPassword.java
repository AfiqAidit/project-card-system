package com.demobank.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Checks the one demo staff password against a stored hash.
 * The plain password is not kept in this class.
 */
public final class StaffPassword {

  /** SHA-256 of the demo password. */
  private static final byte[] PASSWORD_HASH =
      HexFormat.of()
          .parseHex("2a97516c354b68848cdbd8f54a226a0a55b21ed138e207ad6c5cbb9c00aa5aea");

  private StaffPassword() {}

  public static boolean matches(String username, String password) {
    if (!StaffSession.DEMO_USER.equals(username) || password == null) {
      return false;
    }
    byte[] actual = sha256(password.getBytes(StandardCharsets.UTF_8));
    return MessageDigest.isEqual(PASSWORD_HASH, actual);
  }

  private static byte[] sha256(byte[] input) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(input);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is required", ex);
    }
  }
}
