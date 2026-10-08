package com.demobank.domain;

public record AuthorizationResult(boolean success, DeclineReason declineReason, String message) {

  public static AuthorizationResult approved() {
    return new AuthorizationResult(true, null, "Approved");
  }

  public static AuthorizationResult declined(DeclineReason reason, String message) {
    return new AuthorizationResult(false, reason, message);
  }
}
