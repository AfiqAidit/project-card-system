package com.demobank.web;

import com.demobank.service.CardService;
import com.demobank.service.ConcurrencyService;
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

@WebServlet(name = "ConcurrencyServlet", urlPatterns = "/concurrency")
public class ConcurrencyServlet extends HttpServlet {

  @EJB
  private CardService cardService;

  @EJB
  private ConcurrencyService concurrencyService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("cards", cardService.listCards());
    req.getRequestDispatcher("/WEB-INF/views/concurrency.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String mode = req.getParameter("mode");
    RequestTrace.start();
    RequestTrace.add(
        "Browser",
        "Form submitted",
        "POST " + req.getContextPath() + "/concurrency · mode=" + mode);
    RequestTrace.add(
        "Servlet",
        "ConcurrencyServlet.doPost()",
        "Starts N async EJB jobs, waits on each Future, then shows result");
    try {
      UUID cardId = UUID.fromString(req.getParameter("cardId"));
      BigDecimal amount = new BigDecimal(req.getParameter("amount"));
      int attempts = Integer.parseInt(req.getParameter("attempts"));
      ConcurrencyRunResult result =
          "safe".equals(mode)
              ? concurrencyService.runSafe(cardId, amount, attempts)
              : concurrencyService.runUnsafe(cardId, amount, attempts);
      req.setAttribute("result", result);
    } catch (Exception ex) {
      req.setAttribute("error", ex.getMessage());
      RequestTrace.add("Servlet", "Error", ex.getMessage());
    }
    RequestTrace.add("Servlet", "Forward to JSP", "Result + optional trace panel");
    req.setAttribute("traceSteps", RequestTrace.finish());
    req.setAttribute("cards", cardService.listCards());
    req.getRequestDispatcher("/WEB-INF/views/concurrency.jsp").forward(req, resp);
  }
}
