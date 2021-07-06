
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChangeAddressResponse {

    @SerializedName("suburbId")
    public String suburbId;

    @SerializedName("hasDeliverySlotReservations")
    public Boolean hasDeliverySlotReservations;

    @SerializedName("unDeliverableCommerceItems")
    public List<Object> unDeliverableCommerceItems = null;

    @SerializedName("stores")
    public String stores;

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

    @SerializedName("quantityLimit")
    public QuantityLimit quantityLimit;

    @SerializedName("unSellableCommerceItems")
    public List<UnSellableCommerceItem> unSellableCommerceItems = null;

    @SerializedName("deliveryStatus")
    public DeliveryStatus deliveryStatus;

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

    public String getStores() {
        return stores;
    }

    public void setStores(String stores) {
        this.stores = stores;
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

    public QuantityLimit getQuantityLimit() {
        return quantityLimit;
    }

    public void setQuantityLimit(QuantityLimit quantityLimit) {
        this.quantityLimit = quantityLimit;
    }

    public List<UnSellableCommerceItem> getUnSellableCommerceItems() {
        return unSellableCommerceItems;
    }

    public void setUnSellableCommerceItems(List<UnSellableCommerceItem> unSellableCommerceItems) {
        this.unSellableCommerceItems = unSellableCommerceItems;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
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
