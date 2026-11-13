<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Staff login · Demo Bank</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/demobank.css?v=22">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/teller.css?v=22">
</head>
<body>
  <div class="db-shell db-shell-auth">
    <header class="db-topbar">
      <div>
        <p class="db-kicker">Demo Bank</p>
        <h1>Staff login</h1>
        <p class="db-lead muted">The back office is for bank staff. The teller desk stays open without a login.</p>
      </div>
      <nav class="db-nav" aria-label="Demo Bank">
        <a href="${pageContext.request.contextPath}/">Home</a>
        <a href="${pageContext.request.contextPath}/teller">Teller</a>
        <a href="${pageContext.request.contextPath}/office/cards.xhtml" aria-current="page">Back office</a>
        <a class="db-nav-quiet" href="${pageContext.request.contextPath}/concurrency">Lab</a>
      </nav>
    </header>

    <main class="db-main">
      <c:if test="${not empty error}">
        <p class="db-flash is-error" role="alert">${error}</p>
      </c:if>

      <section class="panel">
        <h2>Demo account</h2>
        <p class="muted">Username <code>staff</code> · password <code>demo</code>. Fictional, shown on purpose.</p>
        <form class="db-form" method="post" action="${pageContext.request.contextPath}/login">
          <label>Username
            <input class="db-field" name="username" autocomplete="username" value="staff" required>
          </label>
          <label>Password
            <input class="db-field" name="password" type="password" autocomplete="current-password" value="demo" required>
          </label>
          <button type="submit" class="db-btn db-btn-primary">Sign in</button>
        </form>
      </section>

      <c:if test="${not empty traceSteps}">
        <section class="panel trace-panel">
          <h2>What happened</h2>
          <ol class="trace-list">
            <c:forEach var="step" items="${traceSteps}">
              <li>
                <span class="trace-layer trace-layer-${step.layer}">${step.layer}</span>
                <strong>${step.title}</strong>
                <span class="trace-detail">${step.detail}</span>
              </li>
            </c:forEach>
          </ol>
        </section>
      </c:if>
    </main>
  </div>
</body>
</html>
