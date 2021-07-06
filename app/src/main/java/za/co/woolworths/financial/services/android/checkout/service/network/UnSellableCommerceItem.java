
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

public class UnSellableCommerceItem {

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("productId")
    private String productId;

    @SerializedName("displayCategory")
    private String displayCategory;

    @SerializedName("internalImageURL")
    private String internalImageURL;

    @SerializedName("commerceItemClassType")
    private String commerceItemClassType;

    @SerializedName("colour")
    private String colour;

    @SerializedName("detailPageURL")
    private String detailPageURL;

    @SerializedName("size")
    private String size;

    @SerializedName("productVariant")
    private String productVariant;

    @SerializedName("price")
    private Price price;

    @SerializedName("externalImageURL")
    private String externalImageURL;

    @SerializedName("productDisplayName")
    private String productDisplayName;

    @SerializedName("fulfillerType")
    private String fulfillerType;

    @SerializedName("productType")
    private String productType;


    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDisplayCategory() {
        return displayCategory;
    }

    public void setDisplayCategory(String displayCategory) {
        this.displayCategory = displayCategory;
    }

    public String getInternalImageURL() {
        return internalImageURL;
    }

    public void setInternalImageURL(String internalImageURL) {
        this.internalImageURL = internalImageURL;
    }

    public String getCommerceItemClassType() {
        return commerceItemClassType;
    }

    public void setCommerceItemClassType(String commerceItemClassType) {
        this.commerceItemClassType = commerceItemClassType;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getDetailPageURL() {
        return detailPageURL;
    }

    public void setDetailPageURL(String detailPageURL) {
        this.detailPageURL = detailPageURL;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(String productVariant) {
        this.productVariant = productVariant;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getExternalImageURL() {
        return externalImageURL;
    }

    public void setExternalImageURL(String externalImageURL) {
        this.externalImageURL = externalImageURL;
    }

    public String getProductDisplayName() {
        return productDisplayName;
    }

    public void setProductDisplayName(String productDisplayName) {
        this.productDisplayName = productDisplayName;
    }

    public String getFulfillerType() {
        return fulfillerType;
    }

    public void setFulfillerType(String fulfillerType) {
        this.fulfillerType = fulfillerType;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

}
