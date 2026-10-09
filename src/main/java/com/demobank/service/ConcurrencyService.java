package com.demobank.service;

import com.demobank.persistence.CardKind;
import com.demobank.persistence.JpaSupport;
import com.demobank.trace.RequestTrace;
import com.demobank.web.ConcurrencyRunResult;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ConcurrencyService {

  private static final long FUTURE_TIMEOUT_SECONDS = 120;

  @PersistenceUnit(unitName = "DemoBankPU")
  private EntityManagerFactory emf;

  @EJB
  private AuthorizationService authorizationService;

  @EJB
  private UnsafeCardWriter unsafeCardWriter;

  public ConcurrencyRunResult runSafe(UUID cardId, BigDecimal amount, int parallelAttempts) {
    return runParallel(cardId, amount, parallelAttempts, true);
  }

  public ConcurrencyRunResult runUnsafe(UUID cardId, BigDecimal amount, int parallelAttempts) {
    return runParallel(cardId, amount, parallelAttempts, false);
  }

  private ConcurrencyRunResult runParallel(
      UUID cardId, BigDecimal amount, int parallelAttempts, boolean safe) {
    int attempts = Math.min(Math.max(parallelAttempts, 1), 40);
    RequestTrace.add(
        "EJB",
        "ConcurrencyService.run" + (safe ? "Safe" : "Unsafe") + "()",
        attempts + " parallel jobs · card " + cardId + " · RM " + amount + " each");
    BigDecimal balanceBefore = readDebitBalance(cardId);
    RequestTrace.add(
        "JPA",
        "Read balance before",
        "RM " + balanceBefore + " (one row, debit)");
    int maxFair =
        amount.signum() > 0
            ? balanceBefore.divide(amount, 0, RoundingMode.DOWN).intValue()
            : 0;

    List<Future<Boolean>> futures = new ArrayList<>();
    for (int i = 0; i < attempts; i++) {
      if (safe) {
        futures.add(authorizationService.parallelPurchaseAttempt(cardId, amount));
      } else {
        futures.add(unsafeCardWriter.parallelUnsafeAttempt(cardId, amount));
      }
    }
    RequestTrace.add(
        "EJB",
        "Start @Asynchronous jobs",
        safe
            ? attempts + " × AuthorizationService.parallelPurchaseAttempt"
            : attempts + " × UnsafeCardWriter.parallelUnsafeAttempt");

    int approved = 0;
    int declined = 0;
    for (Future<Boolean> future : futures) {
      try {
        if (Boolean.TRUE.equals(future.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS))) {
          approved++;
        } else {
          declined++;
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        declined++;
      } catch (ExecutionException | TimeoutException ex) {
        declined++;
      }
    }

    RequestTrace.add(
        "EJB",
        "Collect Future results",
        approved + " approved · " + declined + " declined (HTTP thread waited on each get())");
    BigDecimal balanceAfter = readDebitBalance(cardId);
    RequestTrace.add(
        "JPA",
        "Read balance after",
        "RM " + balanceAfter + " · compare with approved × amount");
    return new ConcurrencyRunResult(
        safe ? "safe" : "unsafe",
        attempts,
        approved,
        declined,
        balanceBefore,
        balanceAfter,
        amount,
        maxFair);
  }

  private BigDecimal readDebitBalance(UUID cardId) {
    try {
      return JpaSupport.inTransaction(
          emf,
          em ->
              em.createQuery(
                      "select c.debitBalance from CardEntity c where c.id = :id and c.kind = :kind",
                      BigDecimal.class)
                  .setParameter("id", cardId)
                  .setParameter("kind", CardKind.DEBIT)
                  .getSingleResult());
    } catch (RuntimeException ex) {
      Throwable cause = ex;
      while (cause != null) {
        if (cause instanceof NoResultException) {
          return BigDecimal.ZERO;
        }
        cause = cause.getCause();
      }
      throw ex;
    }
  }
}
