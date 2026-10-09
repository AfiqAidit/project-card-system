package com.demobank.service;

import com.demobank.persistence.CardKind;
import com.demobank.persistence.JpaSupport;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Demonstrates bad concurrent updates (no proper locking). Runs on the server-managed async pool.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class UnsafeCardWriter {

  @PersistenceUnit(unitName = "DemoBankPU")
  private EntityManagerFactory emf;

  @Asynchronous
  public Future<Boolean> parallelUnsafeAttempt(UUID cardId, BigDecimal amount) {
    return new AsyncResult<>(unsafeSubtract(cardId, amount));
  }

  boolean unsafeSubtract(UUID cardId, BigDecimal amount) {
    return JpaSupport.inTransaction(
        emf,
        em -> {
          BigDecimal balance =
              em.createQuery(
                      "select c.debitBalance from CardEntity c where c.id = :id and c.kind = :kind",
                      BigDecimal.class)
                  .setParameter("id", cardId)
                  .setParameter("kind", CardKind.DEBIT)
                  .getSingleResult();
          if (balance == null || balance.compareTo(amount) < 0) {
            return false;
          }
          BigDecimal staleNewBalance = balance.subtract(amount);
          int updated =
              em.createQuery(
                      "update CardEntity c set c.debitBalance = :bal "
                          + "where c.id = :id and c.kind = :kind")
                  .setParameter("bal", staleNewBalance)
                  .setParameter("id", cardId)
                  .setParameter("kind", CardKind.DEBIT)
                  .executeUpdate();
          return updated > 0;
        });
  }
}
