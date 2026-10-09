package com.demobank.service;

import com.demobank.domain.AuthorizationResult;
import com.demobank.domain.Card;
import com.demobank.domain.Money;
import com.demobank.persistence.CardEntity;
import com.demobank.persistence.CardMapper;
import com.demobank.persistence.JpaSupport;
import com.demobank.trace.RequestTrace;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceUnit;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Future;

/** Approves or declines card purchases (separate from issuing cards). */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AuthorizationService {

  private static final int VERSION_RETRY_LIMIT = 12;

  @PersistenceUnit(unitName = "DemoBankPU")
  private EntityManagerFactory emf;

  /** One parallel “terminal” — runs on WildFly’s managed async threads (safe for JPA commits). */
  @Asynchronous
  public Future<Boolean> parallelPurchaseAttempt(UUID cardId, BigDecimal amount) {
    for (int attempt = 0; attempt < VERSION_RETRY_LIMIT; attempt++) {
      AuthorizationResult result = authorize(cardId, amount);
      if (result.success()) {
        return new AsyncResult<>(true);
      }
      String message = result.message() != null ? result.message() : "";
      if (!message.contains("Concurrent")) {
        return new AsyncResult<>(false);
      }
    }
    return new AsyncResult<>(false);
  }

  public AuthorizationResult authorize(UUID cardId, BigDecimal amount) {
    RequestTrace.add(
        "EJB",
        "AuthorizationService.authorize()",
        "@Stateless payment bean · card " + cardId + " · RM " + amount);
    try {
      return JpaSupport.inTransaction(
          emf,
          em -> {
            CardEntity entity = em.find(CardEntity.class, cardId);
            if (entity == null) {
              RequestTrace.add("Domain", "Card not found", "No row for that id");
              return AuthorizationResult.declined(null, "Card not found");
            }
            Card card = CardMapper.toDomain(entity);
            RequestTrace.add(
                "Domain",
                card.getClass().getSimpleName() + ".authorize()",
                "Business rules in Java (balance, limit, frozen status)");
            AuthorizationResult result = card.authorize(Money.of(amount));
            CardMapper.applyDomainState(entity, card);
            RequestTrace.add(
                "JPA",
                "UPDATE cards",
                result.success()
                    ? "Approved · balance/limit saved (version column guards concurrent updates)"
                    : "Declined · row unchanged except status if needed");
            return result;
          });
    } catch (RuntimeException ex) {
      if (isOptimisticLock(ex)) {
        RequestTrace.add(
            "JPA",
            "Optimistic lock conflict",
            "Another request updated this card first · this purchase was not saved");
        return AuthorizationResult.declined(null, "Concurrent update — try again");
      }
      throw ex;
    }
  }

  private static boolean isOptimisticLock(Throwable ex) {
    for (Throwable t = ex; t != null; t = t.getCause()) {
      if (t instanceof OptimisticLockException) {
        return true;
      }
    }
    return false;
  }
}
