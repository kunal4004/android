
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

public class ConfirmSelectionResponse {

    @SerializedName("productCountMap")
    private ProductCountMap productCountMap;

    @SerializedName("response")
    private Response response;

    @SerializedName("httpCode")
    private Integer httpCode;

    public ProductCountMap getProductCountMap() {
        return productCountMap;
    }

    public void setProductCountMap(ProductCountMap productCountMap) {
        this.productCountMap = productCountMap;
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
