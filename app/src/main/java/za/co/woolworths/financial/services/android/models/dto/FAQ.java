package za.co.woolworths.financial.services.android.models.dto;


import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FAQ {

    @SerializedName("faqs")
    @Expose
    public List<FAQDetail> faqs = null;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;
    @SerializedName("response")
    @Expose
    public Response response;

}