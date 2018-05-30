package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/11.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubCategory {

    @SerializedName("categoryName")
    @Expose
    public String categoryName;
    @SerializedName("categoryId")
    @Expose
    public String categoryId;
    @SerializedName("hasChildren")
    @Expose
    public Boolean hasChildren;

}