package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/12.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PagingResponse {

    @SerializedName("pageSize")
    @Expose
    public Integer pageSize;
    @SerializedName("pageOffset")
    @Expose
    public Integer pageOffset;
    @SerializedName("numItemsOnPage")
    @Expose
    public Integer numItemsOnPage;
    @SerializedName("numItemsInTotal")
    @Expose
    public Integer numItemsInTotal;
    @SerializedName("numPages")
    @Expose
    public Integer numPages;

}
