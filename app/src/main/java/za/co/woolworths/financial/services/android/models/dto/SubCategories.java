package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/11.
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubCategories {

    @SerializedName("subCategories")
    @Expose
    public List<SubCategory> subCategories = null;
    @SerializedName("response")
    @Expose
    public Response response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;

}
