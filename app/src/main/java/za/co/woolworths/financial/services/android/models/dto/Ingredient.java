package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ingredient {

    @SerializedName("ingredientQuantity")
    @Expose
    public String ingredientQuantity;
    @SerializedName("ingredientUOM")
    @Expose
    public String ingredientUOM;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("ingredient")
    @Expose
    public Ingredient_ ingredient;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("item-id")
    @Expose
    public String itemId;

}