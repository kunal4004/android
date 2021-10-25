
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Nullable;

import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem;

public class ChangeAddressResponse {

    @SerializedName("suburbId")
    public String suburbId;

    @SerializedName("unDeliverableCommerceItems")
    public List<Object> unDeliverableCommerceItems = null;

    @SerializedName("deliverable")
    @Nullable
    public Boolean deliverable;

    @SerializedName("firstAvailableFoodDeliveryDate")
    public String firstAvailableFoodDeliveryDate;

    @SerializedName("firstAvailableOtherDeliveryDate")
    public String firstAvailableOtherDeliveryDate;

    @SerializedName("unSellableCommerceItems")
    public List<UnSellableCommerceItem> unSellableCommerceItems = null;

    @SerializedName("response")
    public Response response;

    @SerializedName("httpCode")
    public Integer httpCode;

    public String getSuburbId() {
        return suburbId;
    }

    public void setSuburbId(String suburbId) {
        this.suburbId = suburbId;
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
