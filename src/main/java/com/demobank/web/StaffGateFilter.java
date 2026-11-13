package com.demobank.web;

import com.demobank.trace.RequestTrace;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Runs before every page. The back office needs a staff session.
 * Public pages (teller, home, login) are left alone.
 */
@WebFilter(filterName = "StaffGateFilter", urlPatterns = "/*")
public class StaffGateFilter implements Filter {

  static final String GATE_NOTE = "demobank.gateNote";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String path = pathWithinApp(req);

    if (isOffice(path)) {
      HttpSession session = req.getSession(false);
      Object staff = session == null ? null : session.getAttribute(StaffSession.USER);
      if (staff == null) {
        resp.sendRedirect(req.getContextPath() + "/login");
        return;
      }
      RequestTrace.start();
      RequestTrace.add(
          "Filter",
          "StaffGateFilter",
          "Ran before the page. Session belongs to " + staff + ".");
    } else {
      req.setAttribute(
          GATE_NOTE,
          "StaffGateFilter ran first. This page is public, so no login was required.");
    }
    chain.doFilter(request, response);
  }

  private static boolean isOffice(String path) {
    return "/office".equals(path) || path.startsWith("/office/");
  }

  private static String pathWithinApp(HttpServletRequest req) {
    String uri = req.getRequestURI();
    String context = req.getContextPath();
    if (context != null && !context.isEmpty() && uri.startsWith(context)) {
      return uri.substring(context.length());
    }
    return uri;
  }
}
