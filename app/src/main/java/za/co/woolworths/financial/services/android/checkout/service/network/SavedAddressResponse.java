
package za.co.woolworths.financial.services.android.checkout.service.network;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SavedAddressResponse {

    @SerializedName("addresses")
    private List<Address> addresses = null;

    @SerializedName("primaryContactNo")
    private String primaryContactNo;

    @SerializedName("defaultAddressNickname")
    private String defaultAddressNickname;

    @SerializedName("isStorePickup")
    private Boolean isStorePickup;

    @SerializedName("response")
    private Response response;

    @SerializedName("httpCode")
    private Integer httpCode;

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public String getPrimaryContactNo() {
        return primaryContactNo;
    }

    public void setPrimaryContactNo(String primaryContactNo) {
        this.primaryContactNo = primaryContactNo;
    }

    public String getDefaultAddressNickname() {
        return defaultAddressNickname;
    }

    public void setDefaultAddressNickname(String defaultAddressNickname) {
        this.defaultAddressNickname = defaultAddressNickname;
    }

    public Boolean getIsStorePickup() {
        return isStorePickup;
    }

    public void setIsStorePickup(Boolean isStorePickup) {
        this.isStorePickup = isStorePickup;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

}
