package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/21.
 */

public class CreditLimit {

    private String title;
    private String description;
    private String amount;

    public CreditLimit(String title, String amount,String description) {
        this.title = title;
        this.amount = amount;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
