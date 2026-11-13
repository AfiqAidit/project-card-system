package com.demobank.web;

import com.demobank.service.AuthorizationService;
import com.demobank.service.CardService;
import com.demobank.trace.RequestTrace;
import com.demobank.trace.TraceStep;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Backing bean for the JSF back office page. */
@Named("office")
@RequestScoped
public class OfficeBean {

  @EJB
  private CardService cardService;

  @EJB
  private AuthorizationService authorizationService;

  private String query = "";
  private String flash;
  private boolean flashError;
  private List<CardSummary> cards;
  private List<PurchaseSummary> purchases;
  private boolean loaded;
  private List<TraceStep> trace;
  private final Map<Object, String> limitDrafts = new HashMap<>();

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getFlash() {
    return flash;
  }

  public boolean isFlashError() {
    return flashError;
  }

  public Map<Object, String> getLimitDrafts() {
    return limitDrafts;
  }

  public String getStaffName() {
    return StaffSession.DEMO_USER;
  }

  public List<CardSummary> getCards() {
    if (!loaded) {
      cards = cardService.search(query);
      loaded = true;
      rememberLimits(cards);
    }
    return cards;
  }

  public List<PurchaseSummary> getPurchases() {
    if (purchases == null) {
      purchases = authorizationService.recentPurchases();
    }
    return purchases;
  }

  public void search() {
    loaded = false;
    RequestTrace.add("JSF", "Search", "Back office reloaded the card table");
  }

  public void freeze(String id) {
    run(() -> cardService.freeze(UUID.fromString(id)), "Card frozen. A purchase on it will be declined.");
  }

  public void unfreeze(String id) {
    run(() -> cardService.unfreeze(UUID.fromString(id)), "Card is active again.");
  }

  public void changeLimit(String id) {
    String raw = draftLimit(id);
    if (raw == null || raw.isBlank()) {
      fail("Enter a new credit limit first.");
      return;
    }
    try {
      BigDecimal amount = new BigDecimal(raw.trim());
      run(() -> cardService.changeCreditLimit(UUID.fromString(id), amount), "Credit limit updated.");
    } catch (NumberFormatException ex) {
      fail("Limit must be a number, like 1000.00.");
    }
  }

  public List<TraceStep> getTraceSteps() {
    if (trace != null) {
      return trace;
    }
    List<TraceStep> current = new ArrayList<>(RequestTrace.finish());
    HttpServletRequest req = request();
    if (req != null) {
      HttpSession session = req.getSession(false);
      if (session != null) {
        Object prior = session.getAttribute(StaffSession.LOGIN_TRACE);
        if (prior instanceof List<?> earlier) {
          session.removeAttribute(StaffSession.LOGIN_TRACE);
          List<TraceStep> merged = new ArrayList<>();
          for (Object step : earlier) {
            if (step instanceof TraceStep traceStep) {
              merged.add(traceStep);
            }
          }
          merged.addAll(current);
          current = merged;
        }
      }
    }
    trace = List.copyOf(current);
    return trace;
  }

  private void rememberLimits(List<CardSummary> rows) {
    if (flashError || rows == null) {
      return;
    }
    for (CardSummary card : rows) {
      if (card.creditLimit() == null) {
        continue;
      }
      String text = card.creditLimit().toPlainString();
      limitDrafts.put(card.id(), text);
    }
  }

  private String draftLimit(String id) {
    String direct = limitDrafts.get(id);
    if (direct != null) {
      return direct;
    }
    for (Map.Entry<Object, String> entry : limitDrafts.entrySet()) {
      if (entry.getKey() != null && id.equals(entry.getKey().toString())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void run(Runnable action, String success) {
    try {
      action.run();
      flash = success;
      flashError = false;
      loaded = false;
    } catch (RuntimeException ex) {
      fail(rootMessage(ex));
    }
  }

  private static String rootMessage(Throwable ex) {
    Throwable current = ex;
    while (current.getCause() != null && current.getCause() != current) {
      current = current.getCause();
    }
    String message = current.getMessage();
    if (message == null || message.isBlank()) {
      return "That change was rejected.";
    }
    return message;
  }

  private void fail(String message) {
    flash = message;
    flashError = true;
    RequestTrace.add("JSF", "Rejected", message);
    loaded = false;
  }

  private static HttpServletRequest request() {
    FacesContext faces = FacesContext.getCurrentInstance();
    if (faces == null) {
      return null;
    }
    Object req = faces.getExternalContext().getRequest();
    if (req instanceof HttpServletRequest http) {
      return http;
    }
    return null;
  }
}
