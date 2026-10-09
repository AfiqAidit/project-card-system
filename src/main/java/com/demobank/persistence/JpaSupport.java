package com.demobank.persistence;

import com.demobank.trace.RequestTrace;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.function.Function;

public final class JpaSupport {

  private JpaSupport() {}

  public static <T> T inTransaction(EntityManagerFactory emf, Function<EntityManager, T> work) {
    SchemaPatches.applyIfNeeded(emf);
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
}
