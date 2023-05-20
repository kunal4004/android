package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request;

import java.io.Serializable;

public class Session implements Serializable {
    private String dy;

    public Session(String dy) {
        this.dy = dy;
    }

    public String getDy() {
        return this.dy;
    }

    public void setDy(String dy) {
        this.dy = dy;
    }
}
