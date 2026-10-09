<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Demo Bank</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/demobank.css?v=16">
</head>
<body>
  <div class="db-shell">
    <header class="db-topbar">
      <div>
        <p class="db-kicker">Portfolio project</p>
        <h1>Demo Bank</h1>
        <p class="db-lead muted">Fictional card system built with Java 21, Jakarta EE, and WildFly.</p>
      </div>
    </header>

    <main class="db-main">
      <section class="panel">
        <h2>Where to go</h2>
        <p class="muted">Start with the teller desk to issue cards and run a test purchase.</p>
        <ul class="db-home">
          <li><a href="${pageContext.request.contextPath}/teller">Teller desk</a></li>
          <li><a href="${pageContext.request.contextPath}/concurrency">Concurrency lab (Phase 3)</a></li>
          <li><a href="${pageContext.request.contextPath}/hello">Hello servlet (Phase 0)</a></li>
        </ul>
      </section>
    </main>

    <p class="db-footnote">Demo only · fictional data · card numbers use prefix 999</p>
  </div>
</body>
</html>
