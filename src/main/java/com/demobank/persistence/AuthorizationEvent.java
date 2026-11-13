package com.demobank.persistence;

import com.demobank.domain.DeclineReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One shop-payment attempt. Stores the card id, never the full card number. */
@Entity
@Table(name = "authorization_events")
public class AuthorizationEvent {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, columnDefinition = "uuid")
  private UUID cardId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private boolean approved;

  @Enumerated(EnumType.STRING)
  @Column(length = 32)
  private DeclineReason declineReason;

  @Column(nullable = false)
  private Instant createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCardId() {
    return cardId;
  }

  public void setCardId(UUID cardId) {
    this.cardId = cardId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public boolean isApproved() {
    return approved;
  }

  public void setApproved(boolean approved) {
    this.approved = approved;
  }

  public DeclineReason getDeclineReason() {
    return declineReason;
  }

  public void setDeclineReason(DeclineReason declineReason) {
    this.declineReason = declineReason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
