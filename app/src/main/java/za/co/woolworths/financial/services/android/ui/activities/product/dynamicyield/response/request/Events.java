package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request;

import java.io.Serializable;

public class Events implements Serializable {
    private String name;

    private Properties properties;

    public Events(String name, Properties properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}