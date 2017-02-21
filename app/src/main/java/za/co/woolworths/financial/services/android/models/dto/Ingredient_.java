package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ingredient_ {

    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("products")
    @Expose
    public List<ProductIngredient> products = null;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("item-id")
    @Expose
    public String itemId;

}