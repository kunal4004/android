package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PromotionImages {

    @SerializedName("wRewards")
    @Expose
    public String wRewards;
    @SerializedName("save")
    @Expose
    public String save;

}
