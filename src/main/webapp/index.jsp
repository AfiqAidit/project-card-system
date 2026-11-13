<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Demo Bank</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/demobank.css?v=22">
</head>
<body>
  <div class="db-shell">
    <header class="db-topbar">
      <div>
        <p class="db-kicker">Portfolio project</p>
        <h1>Demo Bank</h1>
        <p class="db-lead muted">Fictional card system. Two steps: open a card, then manage it as staff.</p>
      </div>
      <nav class="db-nav" aria-label="Demo Bank">
        <a href="${pageContext.request.contextPath}/" aria-current="page">Home</a>
        <a href="${pageContext.request.contextPath}/teller">Teller</a>
        <a href="${pageContext.request.contextPath}/office/cards.xhtml">Back office</a>
        <a class="db-nav-quiet" href="${pageContext.request.contextPath}/concurrency">Lab</a>
      </nav>
    </header>

    <main class="db-main">
      <div class="db-path">
        <a class="db-path-card" href="${pageContext.request.contextPath}/teller">
          <span class="db-step">1</span>
          <span>
            <h2>Teller desk</h2>
            <p>Open a debit or credit card, then try a shop payment. No login.</p>
          </span>
        </a>
        <a class="db-path-card" href="${pageContext.request.contextPath}/office/cards.xhtml">
          <span class="db-step">2</span>
          <span>
            <h2>Back office</h2>
            <p>Sign in as staff (<code>staff</code> / <code>demo</code>), then search, freeze, change a credit limit, or read purchases.</p>
          </span>
        </a>
      </div>
      <p class="db-also">Also in this demo: <a href="${pageContext.request.contextPath}/concurrency">concurrency lab</a> and the <a href="${pageContext.request.contextPath}/hello">hello servlet</a>.</p>
    </main>

    <p class="db-footnote">Demo only · fictional data · card numbers use prefix 999</p>
  </div>
</body>
</html>
