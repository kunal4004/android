package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RootCategory {

    @SerializedName("imgUrl")
    @Expose
    public String imgUrl;

    @SerializedName("categoryName")
    @Expose
    public String categoryName;

    @SerializedName("categoryId")
    @Expose
    public String categoryId;

    @SerializedName("hasChildren")
    @Expose
    public Boolean hasChildren;

    @SerializedName("dimValId")
    @Expose
    public String dimValId;

}