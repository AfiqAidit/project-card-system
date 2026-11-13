package com.demobank.service;

import com.demobank.domain.AuthorizationResult;
import com.demobank.domain.Card;
import com.demobank.domain.CardNumber;
import com.demobank.domain.DeclineReason;
import com.demobank.domain.Money;
import com.demobank.persistence.AuthorizationEvent;
import com.demobank.persistence.CardEntity;
import com.demobank.persistence.CardMapper;
import com.demobank.persistence.JpaSupport;
import com.demobank.trace.RequestTrace;
import com.demobank.web.PurchaseSummary;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/** Approves or declines card purchases (separate from issuing cards). */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AuthorizationService {

  private static final int VERSION_RETRY_LIMIT = 12;
  private static final int RECENT_LIMIT = 20;
  private static final DateTimeFormatter WHEN =
      DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH)
          .withZone(ZoneId.of("Asia/Kuala_Lumpur"));

  @PersistenceUnit(unitName = "DemoBankPU")
  private EntityManagerFactory emf;

  /** One parallel terminal. Runs on WildFly managed async threads (safe for JPA commits). */
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
            Money money = Money.of(amount);
            AuthorizationResult result = card.authorize(money);
            CardMapper.applyDomainState(entity, card);
            AuthorizationEvent event = new AuthorizationEvent();
            event.setId(UUID.randomUUID());
            event.setCardId(cardId);
            event.setAmount(money.amount());
            event.setApproved(result.success());
            event.setDeclineReason(result.declineReason());
            event.setCreatedAt(Instant.now());
            em.persist(event);
            RequestTrace.add(
                "JPA",
                "INSERT into authorization_events",
                (result.success() ? "Approved" : "Declined")
                    + ", RM "
                    + money.amount()
                    + ", card "
                    + card.number().masked());
            RequestTrace.add(
                "JPA",
                "UPDATE cards",
                result.success()
                    ? "Approved. Balance or limit saved (version column guards concurrent updates)"
                    : "Declined. Card row saved with the attempt");
            return result;
          });
    } catch (RuntimeException ex) {
      if (isOptimisticLock(ex)) {
        RequestTrace.add(
            "JPA",
            "Optimistic lock conflict",
            "Another request updated this card first · this purchase was not saved");
        return AuthorizationResult.declined(null, "Concurrent update: try again");
      }
      throw ex;
    }
  }

  public List<PurchaseSummary> recentPurchases() {
    RequestTrace.add(
        "EJB",
        "AuthorizationService.recentPurchases()",
        "Load the latest shop payments for the back office");
    return JpaSupport.inTransaction(
        emf,
        em -> {
          List<AuthorizationEvent> events =
              em.createQuery(
                      "select e from AuthorizationEvent e order by e.createdAt desc",
                      AuthorizationEvent.class)
                  .setMaxResults(RECENT_LIMIT)
                  .getResultList();
          RequestTrace.add(
              "JPA",
              "SELECT from authorization_events",
              events.size() + " row(s), newest first");
          Map<UUID, String> masks = maskedNumbers(em, events);
          List<PurchaseSummary> rows = new ArrayList<>();
          for (AuthorizationEvent event : events) {
            rows.add(
                new PurchaseSummary(
                    WHEN.format(event.getCreatedAt()),
                    masks.getOrDefault(event.getCardId(), "Card removed"),
                    event.getAmount(),
                    event.isApproved(),
                    reasonLabel(event.getDeclineReason())));
          }
          return rows;
        });
  }

  private static Map<UUID, String> maskedNumbers(EntityManager em, List<AuthorizationEvent> events) {
    List<UUID> ids = events.stream().map(AuthorizationEvent::getCardId).distinct().toList();
    Map<UUID, String> masks = new HashMap<>();
    if (ids.isEmpty()) {
      return masks;
    }
    List<CardEntity> cards =
        em.createQuery("select c from CardEntity c where c.id in :ids", CardEntity.class)
            .setParameter("ids", ids)
            .getResultList();
    for (CardEntity card : cards) {
      try {
        masks.put(card.getId(), CardNumber.of(card.getCardNumberDigits()).masked());
      } catch (RuntimeException ex) {
        masks.put(card.getId(), "Card removed");
      }
    }
    return masks;
  }

  private static String reasonLabel(DeclineReason reason) {
    if (reason == null) {
      return "";
    }
    return switch (reason) {
      case INVALID_AMOUNT -> "Invalid amount";
      case CARD_FROZEN -> "Card frozen";
      case INSUFFICIENT_FUNDS -> "Insufficient funds";
      case OVER_CREDIT_LIMIT -> "Over the credit limit";
    };
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
