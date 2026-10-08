package com.demobank.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Steps for the current HTTP request (servlet thread). */
public final class RequestTrace {

  private static final ThreadLocal<List<TraceStep>> STEPS = new ThreadLocal<>();

  private RequestTrace() {}

  public static void start() {
    STEPS.set(new ArrayList<>());
  }

  public static void add(String layer, String title, String detail) {
    List<TraceStep> steps = STEPS.get();
    if (steps != null) {
      steps.add(new TraceStep(layer, title, detail));
    }
  }

  public static List<TraceStep> finish() {
    List<TraceStep> steps = STEPS.get();
    STEPS.remove();
    if (steps == null || steps.isEmpty()) {
      return List.of();
    }
    return Collections.unmodifiableList(steps);
  }
}
