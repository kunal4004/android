package za.co.woolworths.financial.services.android.util;

public class WCreditCardUtil {

    public static CardTypes getCardBucket(String creditCard) {
        if (creditCard.startsWith("486725") ||
                creditCard.startsWith("400154") ||
                creditCard.startsWith("410374") ||
                creditCard.startsWith("410375")) {
            return CardTypes.CREDIT_CARD;
        } else {
            return CardTypes.OTHER;
        }
    }

    public static boolean isWoolworthsCard(String creditCard) {
        return creditCard.startsWith("486725") || creditCard.startsWith("400154") || creditCard.startsWith("410374") || creditCard.startsWith("410375") || creditCard.startsWith("600785");
    }

    public enum CardTypes {
        CREDIT_CARD,
        OTHER
    }
}
