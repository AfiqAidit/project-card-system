package com.demobank.service;

import com.demobank.domain.AuthorizationResult;
import com.demobank.domain.Card;
import com.demobank.domain.CardNumber;
import com.demobank.domain.CreditCard;
import com.demobank.domain.DebitCard;
import com.demobank.domain.Money;
import com.demobank.persistence.CardEntity;
import com.demobank.persistence.CardKind;
import com.demobank.persistence.CardMapper;
import com.demobank.trace.RequestTrace;
import com.demobank.web.CardSummary;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CardService {

  @PersistenceUnit(unitName = "DemoBankPU")
  private EntityManagerFactory emf;

  public CardSummary issueDebit(BigDecimal openingBalance) {
    RequestTrace.add(
        "EJB",
        "CardService.issueDebit()",
        "@Stateless bean · opening balance RM " + openingBalance);
    return inTransaction(
        em -> {
          CardNumber number = nextDemoNumber();
          DebitCard card = new DebitCard(number, Money.of(openingBalance));
          RequestTrace.add(
              "Domain",
              "DebitCard created in memory",
              "Number " + card.number().masked() + " · spends from balance, not credit");
          CardEntity entity = newEntity(card, CardKind.DEBIT);
          entity.setDebitBalance(openingBalance);
          em.persist(entity);
          RequestTrace.add(
              "JPA",
              "INSERT into cards",
              "Hibernate persists CardEntity · file DB ~/.demobank/");
          return toSummary(entity, card);
        });
  }

  public CardSummary issueCredit(BigDecimal limit) {
    RequestTrace.add(
        "EJB",
        "CardService.issueCredit()",
        "@Stateless bean · credit limit RM " + limit);
    return inTransaction(
        em -> {
          CardNumber number = nextDemoNumber();
          CreditCard card = new CreditCard(number, Money.of(limit));
          RequestTrace.add(
              "Domain",
              "CreditCard created in memory",
              "Number " + card.number().masked() + " · limit RM " + limit + ", used starts at 0");
          CardEntity entity = newEntity(card, CardKind.CREDIT);
          entity.setCreditLimit(limit);
          entity.setCreditUsed(BigDecimal.ZERO);
          em.persist(entity);
          RequestTrace.add(
              "JPA",
              "INSERT into cards",
              "Hibernate persists CardEntity · kind=CREDIT");
          return toSummary(entity, card);
        });
  }

  public AuthorizationResult authorize(UUID cardId, BigDecimal amount) {
    RequestTrace.add(
        "EJB",
        "CardService.authorize()",
        "Card id " + cardId + " · purchase RM " + amount);
    return inTransaction(
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
              "Checks status, then balance/limit rules in Java (not SQL)");
          AuthorizationResult result = card.authorize(Money.of(amount));
          CardMapper.applyDomainState(entity, card);
          RequestTrace.add(
              "JPA",
              "UPDATE cards",
              result.success() ? "Approved · balance/limit saved" : "Declined · no spend applied");
          return result;
        });
  }

  public List<CardSummary> listCards() {
    RequestTrace.add("EJB", "CardService.listCards()", "Load all cards for the table");
    return inTransaction(
        em -> {
          List<CardEntity> rows =
              em.createQuery("select c from CardEntity c order by c.cardNumberDigits", CardEntity.class)
                  .getResultList();
          RequestTrace.add(
              "JPA",
              "SELECT from cards",
              "JPQL query · " + rows.size() + " row(s) · H2 file ~/.demobank/");
          return rows.stream().map(e -> toSummary(e, CardMapper.toDomain(e))).toList();
        });
  }

  private <T> T inTransaction(Function<EntityManager, T> work) {
    EntityManager em = emf.createEntityManager();
    var tx = em.getTransaction();
    RequestTrace.add("JPA", "Begin transaction", "EntityManager (RESOURCE_LOCAL)");
    tx.begin();
    try {
      T result = work.apply(em);
      tx.commit();
      RequestTrace.add("JPA", "Commit", "Changes written to the database");
      return result;
    } catch (RuntimeException ex) {
      if (tx.isActive()) {
        tx.rollback();
        RequestTrace.add("JPA", "Rollback", ex.getMessage());
      }
      throw ex;
    } finally {
      em.close();
    }
  }

  private static CardEntity newEntity(Card card, CardKind kind) {
    CardEntity entity = new CardEntity();
    entity.setId(card.id());
    entity.setCardNumberDigits(card.number().digitsForStorage());
    entity.setKind(kind);
    entity.setStatus(card.status());
    return entity;
  }

  private static CardSummary toSummary(CardEntity entity, Card card) {
    return new CardSummary(
        entity.getId(),
        card.number().masked(),
        entity.getKind().name(),
        card.status().name(),
        card.availableToSpend().amount());
  }

  private static CardNumber nextDemoNumber() {
    String suffix = String.format("%012d", Math.abs(System.nanoTime() % 1_000_000_000_000L));
    return CardNumber.generateDemo(suffix);
  }
}
