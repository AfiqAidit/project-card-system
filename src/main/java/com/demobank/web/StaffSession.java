package com.demobank.web;

/** Session keys for the staff back office. */
public final class StaffSession {

  public static final String USER = "demobank.staffUser";
  public static final String LOGIN_TRACE = "demobank.loginTrace";

  /** Shown on the home and login pages. The password is public on purpose. */
  public static final String DEMO_USER = "staff";

  public static final String DEMO_PASSWORD = "demo";

  private StaffSession() {}
}
