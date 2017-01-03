package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/28.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateBankDetailResponse {

    @SerializedName("response")
    @Expose
    public BankResponse response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;

}