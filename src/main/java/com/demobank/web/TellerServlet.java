package com.demobank.web;

import com.demobank.domain.AuthorizationResult;
import com.demobank.service.AuthorizationService;
import com.demobank.service.CardService;
import com.demobank.trace.RequestTrace;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@WebServlet(name = "TellerServlet", urlPatterns = "/teller")
public class TellerServlet extends HttpServlet {

  @EJB
  private CardService cardService;

  @EJB
  private AuthorizationService authorizationService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    RequestTrace.start();
    RequestTrace.add(
        "Browser",
        "You opened the teller page",
        "GET " + req.getContextPath() + "/teller");
    RequestTrace.add(
        "Servlet",
        "TellerServlet.doGet()",
        "Runs on WildFly; forwards to teller.jsp");
    req.setAttribute("cards", cardService.listCards());
    req.setAttribute("traceSteps", RequestTrace.finish());
    req.getRequestDispatcher("/WEB-INF/views/teller.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String action = req.getParameter("action");
    RequestTrace.start();
    RequestTrace.add(
        "Browser",
        "Form submitted",
        "POST " + req.getContextPath() + "/teller · action=" + action);
    RequestTrace.add(
        "Servlet",
        "TellerServlet.doPost()",
        "Issue card → CardService · purchase → AuthorizationService");
    try {
      if ("issueDebit".equals(action)) {
        BigDecimal balance = new BigDecimal(req.getParameter("openingBalance"));
        cardService.issueDebit(balance);
        req.setAttribute("flash", "Debit card created.");
        req.setAttribute("tourAfterAction", action);
      } else if ("issueCredit".equals(action)) {
        BigDecimal limit = new BigDecimal(req.getParameter("creditLimit"));
        cardService.issueCredit(limit);
        req.setAttribute("flash", "Credit card created.");
        req.setAttribute("tourAfterAction", action);
      } else if ("purchase".equals(action)) {
        UUID cardId = UUID.fromString(req.getParameter("cardId"));
        BigDecimal amount = new BigDecimal(req.getParameter("amount"));
        AuthorizationResult result = authorizationService.authorize(cardId, amount);
        if (result.success()) {
          req.setAttribute("flash", "Approved: " + result.message());
        } else {
          req.setAttribute(
              "flash",
              "Declined: " + result.message()
                  + (result.declineReason() != null ? " (" + result.declineReason() + ")" : ""));
        }
        req.setAttribute("tourAfterAction", action);
      }
    } catch (Exception ex) {
      req.setAttribute("flash", "Error: " + ex.getMessage());
      RequestTrace.add("Servlet", "Error", ex.getMessage());
    }
    RequestTrace.add(
        "Servlet",
        "Forward to JSP",
        "Reload card list and show flash message");
    req.setAttribute("cards", cardService.listCards());
    req.setAttribute("traceSteps", RequestTrace.finish());
    req.getRequestDispatcher("/WEB-INF/views/teller.jsp").forward(req, resp);
  }
}
