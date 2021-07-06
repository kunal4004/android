
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

public class Price {

    @SerializedName("amount")
    private Double amount;

    @SerializedName("wasPrice")
    private Integer wasPrice;

    @SerializedName("rawTotalPrice")
    private Double rawTotalPrice;

    @SerializedName("salePrice")
    private Integer salePrice;

    @SerializedName("listPrice")
    private Double listPrice;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getWasPrice() {
        return wasPrice;
    }

    public void setWasPrice(Integer wasPrice) {
        this.wasPrice = wasPrice;
    }

    public Double getRawTotalPrice() {
        return rawTotalPrice;
    }

    public void setRawTotalPrice(Double rawTotalPrice) {
        this.rawTotalPrice = rawTotalPrice;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public Double getListPrice() {
        return listPrice;
    }

    public void setListPrice(Double listPrice) {
        this.listPrice = listPrice;
    }

}
