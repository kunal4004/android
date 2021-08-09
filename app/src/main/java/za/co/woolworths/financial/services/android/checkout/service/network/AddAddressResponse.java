
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

public class AddAddressResponse {

    @SerializedName("address")
    private Address address;

    @SerializedName("response")
    private Response response;

    @SerializedName("httpCode")
    private Integer httpCode;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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
