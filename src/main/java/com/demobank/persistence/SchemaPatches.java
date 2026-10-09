package com.demobank.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.logging.Logger;

/** Keeps file H2 in sync when entities gain new columns (e.g. @Version). */
public final class SchemaPatches {

  private static final Logger LOG = Logger.getLogger(SchemaPatches.class.getName());
  private static volatile boolean applied;

  private SchemaPatches() {}

  public static void applyIfNeeded(EntityManagerFactory emf) {
    if (applied) {
      return;
    }
    synchronized (SchemaPatches.class) {
      if (applied) {
        return;
      }
      EntityManager em = emf.createEntityManager();
      try {
        if (hasVersionColumn(em)) {
          applied = true;
          return;
        }
        runAlter(em, "ALTER TABLE cards ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0");
        if (!hasVersionColumn(em)) {
          runAlter(em, "ALTER TABLE cards ADD COLUMN version BIGINT NOT NULL DEFAULT 0");
        }
        applied = hasVersionColumn(em);
        if (applied) {
          LOG.info("Added cards.version column for optimistic locking");
        } else {
          LOG.warning("Could not add cards.version — delete ~/.demobank/ and redeploy");
        }
      } finally {
        em.close();
      }
    }
  }

  private static boolean hasVersionColumn(EntityManager em) {
    try {
      Object count =
          em.createNativeQuery(
                  "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                      + "WHERE UPPER(TABLE_NAME) = 'CARDS' AND UPPER(COLUMN_NAME) = 'VERSION'")
              .getSingleResult();
      return count instanceof Number && ((Number) count).intValue() > 0;
    } catch (RuntimeException ex) {
      return false;
    }
  }

  private static void runAlter(EntityManager em, String sql) {
    try {
      em.getTransaction().begin();
      em.createNativeQuery(sql).executeUpdate();
      em.getTransaction().commit();
    } catch (RuntimeException ex) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      LOG.fine("Schema patch attempt: " + ex.getMessage());
    }
  }
}
