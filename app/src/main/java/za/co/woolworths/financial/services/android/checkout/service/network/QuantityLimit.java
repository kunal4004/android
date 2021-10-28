
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

public class QuantityLimit {

    @SerializedName("foodMaximumQuantity")
    private Integer foodMaximumQuantity;

    @SerializedName("other")
    private Integer other;

    @SerializedName("foodLayoutColour")
    private String foodLayoutColour;

    @SerializedName("otherLayoutColour")
    private String otherLayoutColour;

    @SerializedName("food")
    private Integer food;

    @SerializedName("otherMaximumQuantity")
    private Integer otherMaximumQuantity;

    public Integer getFoodMaximumQuantity() {
        return foodMaximumQuantity;
    }

    public void setFoodMaximumQuantity(Integer foodMaximumQuantity) {
        this.foodMaximumQuantity = foodMaximumQuantity;
    }

    public Integer getOther() {
        return other;
    }

    public void setOther(Integer other) {
        this.other = other;
    }

    public String getFoodLayoutColour() {
        return foodLayoutColour;
    }

    public void setFoodLayoutColour(String foodLayoutColour) {
        this.foodLayoutColour = foodLayoutColour;
    }

    public String getOtherLayoutColour() {
        return otherLayoutColour;
    }

    public void setOtherLayoutColour(String otherLayoutColour) {
        this.otherLayoutColour = otherLayoutColour;
    }

    public Integer getFood() {
        return food;
    }

    public void setFood(Integer food) {
        this.food = food;
    }

    public Integer getOtherMaximumQuantity() {
        return otherMaximumQuantity;
    }

    public void setOtherMaximumQuantity(Integer otherMaximumQuantity) {
        this.otherMaximumQuantity = otherMaximumQuantity;
    }

}
