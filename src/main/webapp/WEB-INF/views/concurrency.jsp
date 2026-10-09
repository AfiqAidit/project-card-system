<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Concurrency lab · Demo Bank</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/demobank.css?v=20">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/teller.css?v=20">
</head>
<body class="teller-page">
  <div class="db-shell">
    <header class="db-topbar">
      <div>
        <p class="db-kicker">Demo Bank</p>
        <h1>Concurrency lab</h1>
        <p class="db-lead muted">Many payments on one debit card at the same time — safe vs unsafe code.</p>
      </div>
      <div class="db-topbar-links">
        <a class="db-link" href="${pageContext.request.contextPath}/teller">Teller desk</a>
        <a class="db-link" href="${pageContext.request.contextPath}/">Home</a>
      </div>
    </header>

    <main class="db-main teller-main">
      <c:if test="${not empty error}">
        <p class="db-flash is-error" role="alert">${error}</p>
      </c:if>

      <section class="panel">
        <h2>Run experiment</h2>
        <form class="db-form" method="post" action="${pageContext.request.contextPath}/concurrency">
          <label>Debit card
            <select class="db-field" name="cardId" required>
              <option value="">Select debit card</option>
              <c:forEach var="c" items="${cards}">
                <c:if test="${c.kind() == 'DEBIT'}">
                  <option value="${c.id()}">${c.maskedNumber()} · RM ${c.available()} available</option>
                </c:if>
              </c:forEach>
            </select>
          </label>
          <label>Each purchase (MYR)
            <input class="db-field" name="amount" type="number" min="0.01" step="0.01" value="50" required>
          </label>
          <label>Parallel jobs (started together)
            <input class="db-field" name="attempts" type="number" min="2" max="40" value="10" required>
          </label>
          <div class="db-form-actions">
            <button type="submit" name="mode" value="unsafe" class="db-btn db-btn-primary">Run unsafe</button>
            <button type="submit" name="mode" value="safe" class="db-btn db-btn-primary">Run safe</button>
          </div>
        </form>
        <p class="muted">Issue a debit card on the <a href="${pageContext.request.contextPath}/teller">teller desk</a> first.</p>
      </section>

      <c:if test="${not empty result}">
        <section class="panel">
          <h2>Result (${result.mode()})</h2>
          <ul class="db-stats">
            <li><span>Balance before</span><strong>RM ${result.balanceBefore()}</strong></li>
            <li><span>Each attempt</span><strong>RM ${result.amountEach()}</strong></li>
            <li><span>Parallel jobs</span><strong>${result.attempts()}</strong></li>
            <li><span>Approved</span><strong>${result.approved()}</strong></li>
            <li><span>Declined</span><strong>${result.declined()}</strong></li>
            <li><span>Balance after</span><strong>RM ${result.balanceAfter()}</strong></li>
          </ul>
          <p class="muted db-result-note">
            Up to <strong>${result.maxFairApprovals()}</strong> × RM ${result.amountEach()} could succeed one-by-one.
            <c:choose>
              <c:when test="${result.mode() == 'safe'}">
                Safe: balance change should match approved × amount.
              </c:when>
              <c:otherwise>
                Unsafe: approved count can disagree with how much balance actually dropped.
              </c:otherwise>
            </c:choose>
          </p>
          <div class="db-bars" aria-hidden="true">
            <div class="db-bar db-bar-ok" style="width: ${result.attempts() > 0 ? (result.approved() * 100 / result.attempts()) : 0}%"></div>
            <div class="db-bar db-bar-bad" style="width: ${result.attempts() > 0 ? (result.declined() * 100 / result.attempts()) : 0}%"></div>
          </div>

          <div class="db-prefs" style="margin-top: 1rem;">
            <label class="db-check">
              <input type="checkbox" id="showTrace" autocomplete="off" checked>
              Show what happened (this run)
            </label>
          </div>
          <div id="tracePanel" class="trace-panel trace-panel--below">
            <h3 class="trace-panel-title">What happened</h3>
            <c:choose>
              <c:when test="${empty traceSteps}">
                <p class="muted">No steps recorded. Redeploy the latest WAR if you expected a list here.</p>
              </c:when>
              <c:otherwise>
                <ol class="trace-list">
                  <c:forEach var="step" items="${traceSteps}">
                    <li>
                      <span class="trace-layer trace-layer-${step.layer}">${step.layer}</span>
                      <strong>${step.title}</strong>
                      <span class="trace-detail">${step.detail}</span>
                    </li>
                  </c:forEach>
                </ol>
              </c:otherwise>
            </c:choose>
          </div>
        </section>
      </c:if>
    </main>

    <p class="db-footnote">Demo only · fictional data</p>
  </div>

  <script>
    (function () {
      var key = "demobank.showTraceConcurrency";
      var box = document.getElementById("showTrace");
      var panel = document.getElementById("tracePanel");
      if (!box || !panel) return;
      var stored = localStorage.getItem(key);
      box.checked = stored === null ? true : stored === "1";
      function sync() {
        panel.hidden = !box.checked;
        localStorage.setItem(key, box.checked ? "1" : "0");
      }
      box.addEventListener("change", sync);
      sync();
    })();
  </script>
</body>
</html>
