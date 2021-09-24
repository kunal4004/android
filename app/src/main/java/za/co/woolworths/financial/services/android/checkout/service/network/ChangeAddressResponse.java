
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem;

public class ChangeAddressResponse {

    @SerializedName("suburbId")
    public String suburbId;

    @SerializedName("hasDeliverySlotReservations")
    public Boolean hasDeliverySlotReservations;

    @SerializedName("unDeliverableCommerceItems")
    public List<Object> unDeliverableCommerceItems = null;

    @SerializedName("navSuburbDetails")
    public String navSuburbDetails;

    @SerializedName("deliverable")
    public Boolean deliverable;

    @SerializedName("deliverySlotsDetails")
    public String deliverySlotsDetails;

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

    public Boolean getHasDeliverySlotReservations() {
        return hasDeliverySlotReservations;
    }

    public void setHasDeliverySlotReservations(Boolean hasDeliverySlotReservations) {
        this.hasDeliverySlotReservations = hasDeliverySlotReservations;
    }

    public List<Object> getUnDeliverableCommerceItems() {
        return unDeliverableCommerceItems;
    }

    public void setUnDeliverableCommerceItems(List<Object> unDeliverableCommerceItems) {
        this.unDeliverableCommerceItems = unDeliverableCommerceItems;
    }

    public String getNavSuburbDetails() {
        return navSuburbDetails;
    }

    public void setNavSuburbDetails(String navSuburbDetails) {
        this.navSuburbDetails = navSuburbDetails;
    }

    public Boolean getDeliverable() {
        return deliverable;
    }

    public void setDeliverable(Boolean deliverable) {
        this.deliverable = deliverable;
    }

    public String getDeliverySlotsDetails() {
        return deliverySlotsDetails;
    }

    public void setDeliverySlotsDetails(String deliverySlotsDetails) {
        this.deliverySlotsDetails = deliverySlotsDetails;
    }

    public String getFirstAvailableFoodDeliveryDate() {
        return firstAvailableFoodDeliveryDate;
    }

    public void setFirstAvailableFoodDeliveryDate(String firstAvailableFoodDeliveryDate) {
        this.firstAvailableFoodDeliveryDate = firstAvailableFoodDeliveryDate;
    }

    public String getFirstAvailableOtherDeliveryDate() {
        return firstAvailableOtherDeliveryDate;
    }

    public void setFirstAvailableOtherDeliveryDate(String firstAvailableOtherDeliveryDate) {
        this.firstAvailableOtherDeliveryDate = firstAvailableOtherDeliveryDate;
    }

    public List<UnSellableCommerceItem> getUnSellableCommerceItems() {
        return unSellableCommerceItems;
    }

    public void setUnSellableCommerceItems(List<UnSellableCommerceItem> unSellableCommerceItems) {
        this.unSellableCommerceItems = unSellableCommerceItems;
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
