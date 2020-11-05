package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PromotionImages {

    @SerializedName("newImage")
    @Expose
    public String newImage;
    @SerializedName("save")
    @Expose
    public String save;
    @SerializedName("vitality")
    @Expose
    public String vitality;
    @SerializedName("wRewards")
    @Expose
    public String wRewards;
    @SerializedName("freeGift")
    @Expose
    public String freeGift;
    @SerializedName("reduced")
    @Expose
    public String reduced;
}