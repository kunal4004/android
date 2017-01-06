package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/22.
 */

public class IncomeProof {

    private String title;
    private String description;
    private int drawable;

    public IncomeProof(String title, String description, int drawable) {
        this.title = title;
        this.description = description;
        this.drawable = drawable;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDrawable() {
        return drawable;
    }
}
