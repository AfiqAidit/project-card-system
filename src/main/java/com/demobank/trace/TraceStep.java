package com.demobank.trace;

/** One line in the "Show what happened" panel on the teller page. */
public final class TraceStep {

  private final String layer;
  private final String title;
  private final String detail;

  public TraceStep(String layer, String title, String detail) {
    this.layer = layer;
    this.title = title;
    this.detail = detail;
  }

  public String getLayer() {
    return layer;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }
}
