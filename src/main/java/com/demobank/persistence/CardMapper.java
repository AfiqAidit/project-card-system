package com.demobank.persistence;

import com.demobank.domain.Card;
import com.demobank.domain.CardNumber;
import com.demobank.domain.CreditCard;
import com.demobank.domain.DebitCard;
import com.demobank.domain.Money;
import java.math.BigDecimal;

public final class CardMapper {

  private CardMapper() {}

  public static Card toDomain(CardEntity entity) {
    CardNumber number = CardNumber.of(entity.getCardNumberDigits());
    if (entity.getKind() == CardKind.DEBIT) {
      DebitCard card =
          new DebitCard(
              number,
              Money.of(entity.getDebitBalance() != null ? entity.getDebitBalance() : BigDecimal.ZERO),
              entity.getId());
      card.restoreState(entity.getStatus());
      return card;
    }
    CreditCard card =
        new CreditCard(
            number,
            Money.of(entity.getCreditLimit() != null ? entity.getCreditLimit() : BigDecimal.ZERO),
            entity.getId());
    card.restoreState(
        entity.getStatus(),
        Money.of(entity.getCreditUsed() != null ? entity.getCreditUsed() : BigDecimal.ZERO));
    return card;
  }

  public static void applyDomainState(CardEntity entity, Card card) {
    entity.setStatus(card.status());
    if (card instanceof DebitCard debit) {
      entity.setDebitBalance(debit.balance().amount());
    } else if (card instanceof CreditCard credit) {
      entity.setCreditUsed(credit.amountUsed().amount());
    }
  }
}
