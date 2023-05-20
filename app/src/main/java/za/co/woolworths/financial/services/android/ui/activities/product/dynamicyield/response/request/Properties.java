package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request;

import java.io.Serializable;

public class Properties implements Serializable {
    private String keywords;

    private String dyType;

    public Properties(String keywords, String dyType) {
        this.keywords = keywords;
        this.dyType = dyType;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDyType() {
        return this.dyType;
    }

    public void setDyType(String dyType) {
        this.dyType = dyType;
    }
}
