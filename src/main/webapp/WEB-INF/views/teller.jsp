<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Teller · Demo Bank</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/demobank.css?v=16">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/teller.css?v=16">
</head>
<body class="teller-page"<c:if test="${not empty tourAfterAction}"> data-tour-after-action="${tourAfterAction}"</c:if>>
  <div class="db-shell">
    <header class="db-topbar">
      <div>
        <p class="db-kicker">Demo Bank</p>
        <h1>Teller desk</h1>
        <p class="db-lead muted">Fictional cards (999 prefix). You play the teller: open cards and simulate shop payments.</p>
      </div>
      <div class="db-topbar-links">
        <a class="db-link" href="${pageContext.request.contextPath}/concurrency">Concurrency lab</a>
        <a class="db-link" href="${pageContext.request.contextPath}/">Home</a>
      </div>
    </header>

    <div id="tellerTourAnchor" class="teller-tour-anchor" aria-hidden="true"></div>

    <div class="teller-workspace" id="tellerWorkspace">
      <div class="teller-main">
        <c:if test="${not empty flash}">
          <p class="db-flash<c:if test="${fn:startsWith(flash, 'Error:')}"> is-error</c:if>" id="tour-flash" role="status">${flash}</p>
        </c:if>

        <div class="db-prefs">
          <p class="db-prefs-title">View options</p>
          <label class="db-check">
            <input type="checkbox" id="showTour" autocomplete="off" checked>
            Show processing tips
          </label>
          <label class="db-check" id="tour-trace">
            <input type="checkbox" id="showTrace" autocomplete="off">
            Show what happened
          </label>
        </div>

          <section class="grid" id="tour-open-cards">
            <div class="panel" id="tour-issue-debit">
              <h2>Open debit card</h2>
              <form class="db-form" method="post" action="${pageContext.request.contextPath}/teller">
                <input type="hidden" name="action" value="issueDebit">
                <label>Starting balance (MYR)
                  <input class="db-field" name="openingBalance" type="number" min="0" step="0.01" value="500" required>
                </label>
                <button type="submit" id="tour-submit-debit" class="db-btn db-btn-primary tour-action-btn">Create debit card</button>
              </form>
            </div>
            <div class="panel" id="tour-issue-credit">
              <h2>Open credit card</h2>
              <form class="db-form" method="post" action="${pageContext.request.contextPath}/teller">
                <input type="hidden" name="action" value="issueCredit">
                <label>Spending limit (MYR)
                  <input class="db-field" name="creditLimit" type="number" min="0" step="0.01" value="1000" required>
                </label>
                <button type="submit" id="tour-submit-credit" class="db-btn db-btn-primary tour-action-btn">Create credit card</button>
              </form>
            </div>
          </section>

          <section class="panel" id="tour-purchase">
            <h2>Simulate shop payment</h2>
            <p class="muted">A shop asks the bank: may this card pay this amount? You see approve or decline.</p>
            <form class="db-form" method="post" action="${pageContext.request.contextPath}/teller">
              <input type="hidden" name="action" value="purchase">
              <label>Card
                <select class="db-field" name="cardId" required>
                  <option value="">Choose a card</option>
                  <c:forEach var="c" items="${cards}">
                    <option value="${c.id()}">${c.maskedNumber()} (${c.kind()}, ${c.status()})</option>
                  </c:forEach>
                </select>
              </label>
              <label>Payment amount (MYR)
                <input class="db-field" name="amount" type="number" min="0.01" step="0.01" value="50" required>
              </label>
              <button type="submit" id="tour-submit-purchase" class="db-btn db-btn-primary tour-action-btn">Pay</button>
            </form>
          </section>

      <section class="panel" id="tour-cards">
        <h2>Cards on file</h2>
        <c:choose>
          <c:when test="${empty cards}">
            <p class="muted">No cards yet. Create one above.</p>
          </c:when>
          <c:otherwise>
            <div class="db-table-wrap">
              <table class="db-table">
                <thead>
                  <tr><th>Number</th><th>Type</th><th>Status</th><th>Available (MYR)</th></tr>
                </thead>
                <tbody id="tour-cards-body">
                  <c:forEach var="c" items="${cards}" varStatus="st">
                    <tr<c:if test="${st.last}"> id="tour-latest-card"</c:if>>
                      <td><code>${c.maskedNumber()}</code></td>
                      <td>
                        <c:choose>
                          <c:when test="${c.kind() == 'DEBIT'}"><span class="db-badge db-badge-debit">Debit</span></c:when>
                          <c:otherwise><span class="db-badge db-badge-credit">Credit</span></c:otherwise>
                        </c:choose>
                      </td>
                      <td><span class="db-badge db-badge-active">${c.status()}</span></td>
                      <td>${c.available()}</td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>
      </section>
      </div>

      <aside id="traceAside" class="teller-trace-aside" hidden>
        <section id="tracePanel" class="panel trace-panel">
          <h2>What happened</h2>
          <c:choose>
            <c:when test="${empty traceSteps}">
              <p class="muted trace-empty">Load the page or click a button to see steps here.</p>
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
        </section>

        <section id="traceKeywordsPanel" class="panel trace-keywords-panel" aria-label="Layer keywords">
          <h2>Keywords</h2>
          <p class="muted trace-keywords-intro">Colored tags in the log match these layers.</p>
          <ul class="trace-glossary-list">
            <li>
              <span class="trace-layer trace-layer-Browser">Browser</span>
              User interaction <span class="trace-glossary-hint">(Client)</span>
            </li>
            <li>
              <span class="trace-layer trace-layer-Servlet">Servlet</span>
              Handle incoming requests <span class="trace-glossary-hint">(Endpoint)</span>
            </li>
            <li>
              <span class="trace-layer trace-layer-EJB">EJB</span>
              Perform business logic and application operations <span class="trace-glossary-hint">(Service)</span>
            </li>
            <li>
              <span class="trace-layer trace-layer-Domain">Domain</span>
              Represent business objects and concepts <span class="trace-glossary-hint">(DebitCard, CreditCard)</span>
            </li>
            <li>
              <span class="trace-layer trace-layer-JPA">JPA</span>
              Manage Java objects and database data <span class="trace-glossary-hint">(CardEntity in H2)</span>
            </li>
          </ul>
        </section>
      </aside>
    </div>
  </div>

  <div id="tourRoot" class="tour-root" hidden aria-hidden="true">
    <div id="tourBackdrop" class="tour-backdrop"></div>
    <div id="tourSpotlight" class="tour-spotlight"></div>
    <div id="tourTooltip" class="tour-card" role="dialog" aria-labelledby="tourTitle">
      <div class="tour-arrow" id="tourArrow" aria-hidden="true"></div>
      <p class="tour-kicker" id="tourStepLabel"></p>
      <h2 class="tour-card-title" id="tourTitle"></h2>
      <p class="tour-card-body" id="tourBody"></p>
      <div class="tour-card-footer">
        <button type="button" class="tour-skip" id="tourSkipAll">Skip all</button>
        <button type="button" class="tour-next" id="tourNext">Next</button>
      </div>
      <button type="button" class="tour-muted-link" id="tourDontShow">Do not show these hints</button>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/teller-tour.js?v=16"></script>
  <script>
    (function () {
      var key = "demobank.showTrace";
      var box = document.getElementById("showTrace");
      var aside = document.getElementById("traceAside");
      var workspace = document.getElementById("tellerWorkspace");
      if (!box || !aside || !workspace) return;
      box.checked = localStorage.getItem(key) === "1";
      function sync() {
        var on = box.checked;
        aside.hidden = !on;
        workspace.classList.toggle("teller-workspace--trace-on", on);
        localStorage.setItem(key, on ? "1" : "0");
      }
      box.addEventListener("change", sync);
      sync();
    })();
  </script>
</body>
</html>
