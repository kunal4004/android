package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request;

import java.io.Serializable;

public class User implements Serializable {
    private String dyid_server;

    private String dyid;

    public User(String dyid_server, String dyid) {
        this.dyid_server = dyid_server;
        this.dyid = dyid;
    }

    public String getDyid_server() {
        return this.dyid_server;
    }

    public void setDyid_server(String dyid_server) {
        this.dyid_server = dyid_server;
    }

    public String getDyid() {
        return this.dyid;
    }

    public void setDyid(String dyid) {
        this.dyid = dyid;
    }
}
