package com.demobank.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

@Singleton
@Startup
public class SchemaBootstrap {

  @PersistenceUnit(unitName = "DemoBankPU")
  private EntityManagerFactory emf;

  @PostConstruct
  void onStartup() {
    SchemaPatches.applyIfNeeded(emf);
  }
}
