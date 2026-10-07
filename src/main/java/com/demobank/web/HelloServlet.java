package com.demobank.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** Phase 0: proves WildFly can run our servlet. */
@WebServlet(name = "HelloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html;charset=UTF-8");
    try (PrintWriter out = resp.getWriter()) {
      out.println("<!DOCTYPE html>");
      out.println("<html lang=\"en\"><head><meta charset=\"UTF-8\">");
      out.println("<title>Demo Bank</title></head><body>");
      out.println("<h1>Demo Bank card system</h1>");
      out.println("<p>Servlet is running on WildFly.</p>");
      out.println("<p><small>Demo · fictional data</small></p>");
      out.println("</body></html>");
    }
  }
}
