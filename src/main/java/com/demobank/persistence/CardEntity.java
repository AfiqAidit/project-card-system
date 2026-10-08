package com.demobank.persistence;

import com.demobank.domain.CardStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class CardEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, length = 16)
  private String cardNumberDigits;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private CardKind kind;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private CardStatus status;

  @Column(precision = 19, scale = 2)
  private BigDecimal debitBalance;

  @Column(precision = 19, scale = 2)
  private BigDecimal creditLimit;

  @Column(precision = 19, scale = 2)
  private BigDecimal creditUsed;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCardNumberDigits() {
    return cardNumberDigits;
  }

  public void setCardNumberDigits(String cardNumberDigits) {
    this.cardNumberDigits = cardNumberDigits;
  }

  public CardKind getKind() {
    return kind;
  }

  public void setKind(CardKind kind) {
    this.kind = kind;
  }

  public CardStatus getStatus() {
    return status;
  }

  public void setStatus(CardStatus status) {
    this.status = status;
  }

  public BigDecimal getDebitBalance() {
    return debitBalance;
  }

  public void setDebitBalance(BigDecimal debitBalance) {
    this.debitBalance = debitBalance;
  }

  public BigDecimal getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(BigDecimal creditLimit) {
    this.creditLimit = creditLimit;
  }

  public BigDecimal getCreditUsed() {
    return creditUsed;
  }

  public void setCreditUsed(BigDecimal creditUsed) {
    this.creditUsed = creditUsed;
  }
}
