package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/31.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FAQDetail {

    @SerializedName("answer")
    @Expose
    public String answer;
    @SerializedName("question")
    @Expose
    public String question;

}