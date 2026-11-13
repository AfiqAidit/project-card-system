package com.demobank.service;

import com.demobank.domain.Card;
import com.demobank.domain.CardNumber;
import com.demobank.domain.CreditCard;
import com.demobank.domain.DebitCard;
import com.demobank.domain.Money;
import com.demobank.persistence.AuthorizationEvent;
import com.demobank.persistence.CardEntity;
import com.demobank.persistence.CardKind;
import com.demobank.persistence.CardMapper;
import com.demobank.persistence.JpaSupport;
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
import java.util.Locale;
import java.util.UUID;

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
    return JpaSupport.inTransaction(
        emf,
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
    return JpaSupport.inTransaction(
        emf,
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

  public List<CardSummary> listCards() {
    RequestTrace.add("EJB", "CardService.listCards()", "Load all cards for the table");
    return JpaSupport.inTransaction(
        emf,
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

  public List<CardSummary> search(String query) {
    String trimmed = query == null ? "" : query.trim();
    RequestTrace.add(
        "EJB",
        "CardService.search()",
        trimmed.isEmpty() ? "No filter, load every card" : "Filter cards by \"" + trimmed + "\"");
    List<CardSummary> all = listCards();
    if (trimmed.isEmpty()) {
      return all;
    }
    String needle = trimmed.toLowerCase(Locale.ROOT);
    return all.stream()
        .filter(
            card ->
                card.maskedNumber().toLowerCase(Locale.ROOT).contains(needle)
                    || card.kind().toLowerCase(Locale.ROOT).contains(needle)
                    || card.status().toLowerCase(Locale.ROOT).contains(needle))
        .toList();
  }

  public void freeze(UUID id) {
    changeStatus(id, true);
  }

  public void unfreeze(UUID id) {
    changeStatus(id, false);
  }

  public void changeCreditLimit(UUID id, BigDecimal newLimit) {
    RequestTrace.add(
        "EJB",
        "CardService.changeCreditLimit()",
        "New limit RM " + newLimit + " for card " + id);
    JpaSupport.inTransaction(
        emf,
        em -> {
          CardEntity entity = requireCard(em, id);
          Card card = CardMapper.toDomain(entity);
          if (!(card instanceof CreditCard credit)) {
            throw new IllegalArgumentException("Only a credit card has a limit");
          }
          credit.changeLimit(Money.of(newLimit));
          RequestTrace.add(
              "Domain",
              "CreditCard.changeLimit()",
              "Limit is now RM " + credit.creditLimit().amount());
          CardMapper.applyDomainState(entity, card);
          RequestTrace.add("JPA", "UPDATE cards", "credit limit saved");
          return null;
        });
  }

  private void changeStatus(UUID id, boolean freeze) {
    RequestTrace.add(
        "EJB",
        freeze ? "CardService.freeze()" : "CardService.unfreeze()",
        "Load the card, apply the rule, save the status");
    JpaSupport.inTransaction(
        emf,
        em -> {
          CardEntity entity = requireCard(em, id);
          Card card = CardMapper.toDomain(entity);
          if (freeze) {
            card.freeze();
          } else {
            card.unfreeze();
          }
          RequestTrace.add(
              "Domain",
              card.getClass().getSimpleName() + (freeze ? ".freeze()" : ".unfreeze()"),
              "Status is now " + card.status().name());
          CardMapper.applyDomainState(entity, card);
          RequestTrace.add("JPA", "UPDATE cards", "status = " + card.status().name());
          return null;
        });
  }

  private static CardEntity requireCard(EntityManager em, UUID id) {
    CardEntity entity = em.find(CardEntity.class, id);
    if (entity == null) {
      throw new IllegalArgumentException("Card not found");
    }
    return entity;
  }

  /** Used by the nightly EJB timer; not traced. */
  public int deleteAllCards() {
    return JpaSupport.inTransaction(
        emf,
        em -> {
          em.createQuery("delete from AuthorizationEvent").executeUpdate();
          return em.createQuery("delete from CardEntity").executeUpdate();
        });
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
    BigDecimal limit = card instanceof CreditCard credit ? credit.creditLimit().amount() : null;
    return new CardSummary(
        entity.getId(),
        card.number().masked(),
        entity.getKind().name(),
        card.status().name(),
        card.availableToSpend().amount(),
        limit);
  }

  private static CardNumber nextDemoNumber() {
    String suffix = String.format("%012d", Math.abs(System.nanoTime() % 1_000_000_000_000L));
    return CardNumber.generateDemo(suffix);
  }
}
