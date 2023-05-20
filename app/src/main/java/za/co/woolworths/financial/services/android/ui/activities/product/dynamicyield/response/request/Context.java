package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request;

import java.io.Serializable;

public class Context implements Serializable {
    private Device device;

    public Context(Device device) {
        this.device = device;
    }

    public Device getDevice() {

        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
