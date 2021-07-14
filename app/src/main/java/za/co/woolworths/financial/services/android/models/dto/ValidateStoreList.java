package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import za.co.woolworths.financial.services.android.checkout.service.network.DeliveryStatus;

/**
 * Created by Kunal Uttarwar on 14/07/21.
 */
public class ValidateStoreList {

    @SerializedName("unDeliverableCommerceItems")
    private List<Object> unDeliverableCommerceItems = null;
    @SerializedName("storeAddress")
    private String storeAddress;
    @SerializedName("quantityLimit")
    private QuantityLimit quantityLimit;
    @SerializedName("navSuburbDetails")
    private String navSuburbDetails;
    @SerializedName("deliverable")
    private Boolean deliverable;
    @SerializedName("storeName")
    private String storeName;
    @SerializedName("storeId")
    private String storeId;
    @SerializedName("deliverySlotsDetails")
    private String deliverySlotsDetails;
    @SerializedName("unSellableCommerceItems")
    private List<Object> unSellableCommerceItems = null;
    @SerializedName("deliveryStatus")
    private DeliveryStatus deliveryStatus;
    @SerializedName("firstAvailableFoodDeliveryDate")
    private String firstAvailableFoodDeliveryDate;

    public List<Object> getUnDeliverableCommerceItems() {
        return unDeliverableCommerceItems;
    }

    public void setUnDeliverableCommerceItems(List<Object> unDeliverableCommerceItems) {
        this.unDeliverableCommerceItems = unDeliverableCommerceItems;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public QuantityLimit getQuantityLimit() {
        return quantityLimit;
    }

    public void setQuantityLimit(QuantityLimit quantityLimit) {
        this.quantityLimit = quantityLimit;
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

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getDeliverySlotsDetails() {
        return deliverySlotsDetails;
    }

    public void setDeliverySlotsDetails(String deliverySlotsDetails) {
        this.deliverySlotsDetails = deliverySlotsDetails;
    }

    public List<Object> getUnSellableCommerceItems() {
        return unSellableCommerceItems;
    }

    public void setUnSellableCommerceItems(List<Object> unSellableCommerceItems) {
        this.unSellableCommerceItems = unSellableCommerceItems;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getFirstAvailableFoodDeliveryDate() {
        return firstAvailableFoodDeliveryDate;
    }

    public void setFirstAvailableFoodDeliveryDate(String firstAvailableFoodDeliveryDate) {
        this.firstAvailableFoodDeliveryDate = firstAvailableFoodDeliveryDate;
    }

}
