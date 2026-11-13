package com.demobank.web;

import com.demobank.trace.RequestTrace;
import com.demobank.trace.TraceStep;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession existing = req.getSession(false);
    if (existing != null && existing.getAttribute(StaffSession.USER) != null) {
      resp.sendRedirect(req.getContextPath() + "/office/cards.xhtml");
      return;
    }
    req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    RequestTrace.start();
    RequestTrace.add(
        "Browser",
        "Username and password posted to /login",
        "POST " + req.getContextPath() + "/login");
    Object gateNote = req.getAttribute(StaffGateFilter.GATE_NOTE);
    RequestTrace.add(
        "Filter",
        "StaffGateFilter",
        gateNote == null ? "Filter ran before this servlet." : gateNote.toString());
    RequestTrace.add(
        "Servlet",
        "LoginServlet.doPost()",
        "Hands the password to the hash check. The password is not written in the trace.");
    boolean ok = StaffPassword.matches(username, password);
    RequestTrace.add(
        "Domain",
        "Password checked against a stored hash",
        ok ? "Hash matched the demo staff account." : "Hash did not match. Nothing was stored.");
    if (!ok) {
      req.setAttribute("error", "That username or password is not the demo account.");
      req.setAttribute("traceSteps", RequestTrace.finish());
      req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
      return;
    }
    HttpSession session = req.getSession(true);
    req.changeSessionId();
    session.setAttribute(StaffSession.USER, StaffSession.DEMO_USER);
    RequestTrace.add(
        "Servlet",
        "Session created",
        "The server remembers you. The browser keeps a session cookie, not the password.");
    List<TraceStep> steps = RequestTrace.finish();
    session.setAttribute(StaffSession.LOGIN_TRACE, steps);
    resp.sendRedirect(req.getContextPath() + "/office/cards.xhtml");
  }
}
