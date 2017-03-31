package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductIngredient {

    @SerializedName("repositoryId")
    @Expose
    public String repositoryId;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("longDescription")
    @Expose
    public String longDescription;
    @SerializedName("displayName")
    @Expose
    public String displayName;

}