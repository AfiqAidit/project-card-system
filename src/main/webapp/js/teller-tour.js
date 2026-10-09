(function () {
  var STORAGE_SHOW = "demobank.showTour";
  var STORAGE_DONE = "demobank.tourDone";
  var STORAGE_RESUME = "demobank.tourResume";
  var STORAGE_INDEX = "demobank.tourIndex";
  var POST_DONE = "post";

  var steps = [
    {
      id: "intro",
      placement: "top-dock",
      title: "Processing tips",
      body:
        "Short tips after you use the teller: forms in the browser, then WildFly runs the request. Turn on What happened for the step list."
    },
    {
      id: "frontend-post",
      targetId: "tour-open-cards",
      placement: "top-dock",
      title: "Frontend: you submit a form",
      body:
        "Create debit card, Create credit card, or Pay sends a POST from the browser. WildFly then runs servlet, EJB, and database code."
    },
    {
      id: "after-response",
      placement: "top-dock",
      title: "Frontend: answer came back",
      body:
        "The green line is the outcome. The table shows cards loaded from H2 after the last trace step (JPA Commit). Nothing runs after commit for that request.",
      afterAction: POST_DONE,
      resolveTarget: resultTarget,
      keepScrollTop: true
    },
    {
      id: "backend-trace",
      targetId: "tour-trace",
      placement: "top-dock",
      title: "Backend: full timeline",
      body:
        "The side panel lists each step for the last request. It ends at JPA Commit. The table is the fresh SELECT that reads what was saved."
    }
  ];

  var index = 0;
  var completedActions = {};

  var tourRoot = document.getElementById("tourRoot");
  var backdrop = document.getElementById("tourBackdrop");
  var spotlight = document.getElementById("tourSpotlight");
  var tooltip = document.getElementById("tourTooltip");
  var arrow = document.getElementById("tourArrow");
  var stepLabel = document.getElementById("tourStepLabel");
  var titleEl = document.getElementById("tourTitle");
  var bodyEl = document.getElementById("tourBody");
  var nextBtn = document.getElementById("tourNext");
  var skipBtn = document.getElementById("tourSkipAll");
  var dontShowBtn = document.getElementById("tourDontShow");
  var showTourBox = document.getElementById("showTour");

  if (!tourRoot || !tooltip || !showTourBox || !spotlight) {
    return;
  }

  document.body.appendChild(tourRoot);

  function resultTarget() {
    return (
      document.getElementById("tour-flash")
      || document.getElementById("tour-latest-card")
      || document.getElementById("tour-cards")
    );
  }

  function showTourEnabled() {
    return localStorage.getItem(STORAGE_SHOW) !== "0";
  }

  function tourFinished() {
    return localStorage.getItem(STORAGE_DONE) === "1";
  }

  function setShowTour(on) {
    localStorage.setItem(STORAGE_SHOW, on ? "1" : "0");
    showTourBox.checked = on;
  }

  function markTourDone() {
    localStorage.setItem(STORAGE_DONE, "1");
    localStorage.removeItem(STORAGE_INDEX);
    localStorage.removeItem(STORAGE_RESUME);
  }

  function clearTourDone() {
    localStorage.removeItem(STORAGE_DONE);
    completedActions = {};
  }

  function markPostDone() {
    completedActions[POST_DONE] = true;
  }

  function stepTarget(step) {
    if (step.resolveTarget) {
      return step.resolveTarget();
    }
    if (step.targetId) {
      return document.getElementById(step.targetId);
    }
    return null;
  }

  function shouldShowStep(step) {
    if (!step.afterAction) {
      return true;
    }
    return !!completedActions[step.afterAction];
  }

  function findStepIndexAfterPost() {
    for (var i = 0; i < steps.length; i += 1) {
      if (steps[i].afterAction === POST_DONE) {
        return i;
      }
    }
    return -1;
  }

  function moveIndex(delta) {
    var i = index + delta;
    while (i >= 0 && i < steps.length) {
      if (shouldShowStep(steps[i])) {
        return i;
      }
      i += delta;
    }
    return delta > 0 ? -1 : 0;
  }

  var focusedEl = null;
  var savedScrollY = 0;

  function lockPageScroll() {
    savedScrollY = window.scrollY || document.documentElement.scrollTop || 0;
    document.body.classList.add("tour-active");
    document.body.style.top = "-" + savedScrollY + "px";
  }

  function unlockPageScroll() {
    document.body.classList.remove("tour-active");
    document.body.style.top = "";
    window.scrollTo(0, savedScrollY);
  }

  function clearFocus() {
    if (focusedEl) {
      focusedEl.classList.remove("tour-target-focus");
      focusedEl = null;
    }
  }

  function setTourOpen(open) {
    if (open) {
      if (!tourRoot.classList.contains("is-open")) {
        lockPageScroll();
      }
      tourRoot.hidden = false;
      tourRoot.setAttribute("aria-hidden", "false");
      tourRoot.classList.add("is-open");
    } else {
      tourRoot.classList.remove("is-open");
      tourRoot.hidden = true;
      tourRoot.setAttribute("aria-hidden", "true");
      tooltip.classList.remove("is-visible", "is-top-dock", "is-center");
      clearFocus();
      unlockPageScroll();
    }
  }

  function hideTour() {
    spotlight.classList.add("is-off");
    setTourOpen(false);
  }

  function scrollToTarget(target) {
    if (!target) {
      return;
    }
    target.scrollIntoView({ behavior: "smooth", block: "center", inline: "nearest" });
  }

  function layoutTopDock(target) {
    tooltip.classList.add("is-visible", "is-top-dock");
    tooltip.classList.remove("is-center");
    tooltip.style.visibility = "hidden";
    tooltip.style.top = "12px";
    tooltip.style.left = "50%";
    tooltip.style.transform = "translateX(-50%)";
    tooltip.style.visibility = "visible";
    arrow.hidden = true;

    clearFocus();
    if (target) {
      var tr = target.getBoundingClientRect();
      var spotPad = 6;
      spotlight.classList.remove("is-off");
      spotlight.style.top = tr.top - spotPad + "px";
      spotlight.style.left = tr.left - spotPad + "px";
      spotlight.style.width = tr.width + spotPad * 2 + "px";
      spotlight.style.height = tr.height + spotPad * 2 + "px";
      target.classList.add("tour-target-focus");
      focusedEl = target;
    } else {
      spotlight.classList.add("is-off");
    }
  }

  function layoutPopover(target, placement) {
    if (placement === "top-dock") {
      layoutTopDock(target);
      return;
    }

    var pad = 14;
    var vh = window.innerHeight;
    var vw = window.innerWidth;

    tooltip.classList.add("is-visible");
    tooltip.classList.remove("is-top-dock");
    tooltip.style.visibility = "hidden";

    clearFocus();
    if (!target || placement === "center") {
      tooltip.classList.add("is-center");
      var pop = tooltip.getBoundingClientRect();
      tooltip.style.top = Math.max(pad, (vh - pop.height) / 2) + "px";
      tooltip.style.left = Math.max(pad, (vw - pop.width) / 2) + "px";
      tooltip.style.visibility = "visible";
      spotlight.classList.add("is-off");
      arrow.hidden = true;
      return;
    }

    layoutTopDock(target);
  }

  function openTracePanel() {
    var traceBox = document.getElementById("showTrace");
    if (traceBox && !traceBox.checked) {
      traceBox.checked = true;
      traceBox.dispatchEvent(new Event("change"));
    }
  }

  function renderStep() {
    var step = steps[index];
    var target = stepTarget(step);

    setTourOpen(true);

    function paint() {
      var highlight = stepTarget(step);
      if (step.id === "backend-trace") {
        openTracePanel();
        highlight = document.getElementById("traceAside") || highlight;
      }
      layoutPopover(highlight, step.placement || "top-dock");
    }

    if (step.id === "backend-trace") {
      window.setTimeout(paint, 400);
    } else {
      paint();
    }

    stepLabel.textContent = "Tip " + (index + 1) + " of " + steps.length;
    titleEl.textContent = step.title;
    bodyEl.textContent = step.body;

    var isLast = moveIndex(1) === -1;
    nextBtn.textContent = isLast ? "Done" : "Next";

    backdrop.style.pointerEvents = "none";
    localStorage.setItem(STORAGE_INDEX, String(index));
  }

  function openTourAt(i) {
    if (!showTourEnabled()) {
      hideTour();
      return;
    }
    index = i;
    renderStep();
  }

  function startTour() {
    if (!showTourEnabled() || tourFinished()) {
      hideTour();
      return;
    }
    openTourAt(0);
  }

  function goNext() {
    var next = moveIndex(1);
    if (next === -1) {
      markTourDone();
      hideTour();
      return;
    }
    index = next;
    renderStep();
  }

  function skipAll() {
    markTourDone();
    hideTour();
  }

  function disableHints() {
    setShowTour(false);
    markTourDone();
    hideTour();
  }

  function wireForms() {
    document.querySelectorAll("form[action*='teller']").forEach(function (form) {
      form.addEventListener("submit", function () {
        if (!showTourEnabled()) {
          return;
        }
        localStorage.setItem(STORAGE_RESUME, "1");
        localStorage.removeItem(STORAGE_DONE);
      });
    });
  }

  function resumeAfterPost() {
    var fromBody = document.body.getAttribute("data-tour-after-action");
    var fromStorage = localStorage.getItem(STORAGE_RESUME);
    if ((!fromBody && fromStorage !== "1") || !showTourEnabled()) {
      return false;
    }
    localStorage.removeItem(STORAGE_RESUME);
    markPostDone();
    openTracePanel();
    var i = findStepIndexAfterPost();
    if (i < 0) {
      return false;
    }
    localStorage.removeItem(STORAGE_DONE);
    openTourAt(i);
    return true;
  }

  showTourBox.checked = showTourEnabled();
  showTourBox.addEventListener("change", function () {
    setShowTour(showTourBox.checked);
    if (showTourBox.checked) {
      clearTourDone();
      startTour();
    } else {
      hideTour();
    }
  });

  nextBtn.addEventListener("click", goNext);
  skipBtn.addEventListener("click", skipAll);
  dontShowBtn.addEventListener("click", disableHints);
  window.addEventListener("resize", function () {
    if (tourRoot.hidden) {
      return;
    }
    var step = steps[index];
    layoutPopover(stepTarget(step), step.placement || "top-dock");
  });

  wireForms();

  if (resumeAfterPost()) {
    return;
  }

  if (showTourEnabled() && !tourFinished()) {
    var saved = parseInt(localStorage.getItem(STORAGE_INDEX) || "0", 10);
    if (!isNaN(saved) && saved > 0 && saved < steps.length) {
      openTourAt(saved);
    } else {
      startTour();
    }
  }
})();
