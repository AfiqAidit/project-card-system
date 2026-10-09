package com.demobank.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.logging.Logger;

/** Resets demo cards every night so local H2 does not grow forever. */
@Singleton
@Startup
public class DemoDataResetTimer {

  private static final Logger LOG = Logger.getLogger(DemoDataResetTimer.class.getName());

  @EJB
  private CardService cardService;

  @PostConstruct
  void logSchedule() {
    LOG.info("Demo data reset scheduled daily at 03:00 server time (non-persistent timer)");
  }

  @Schedule(hour = "3", minute = "0", persistent = false)
  public void resetNightly() {
    int removed = cardService.deleteAllCards();
    LOG.info("Demo data reset removed " + removed + " card row(s)");
  }
}
