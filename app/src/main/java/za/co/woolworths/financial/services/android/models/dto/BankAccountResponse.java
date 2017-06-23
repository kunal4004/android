package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dimitrij on 2016/12/22.
 */

public class BankAccountResponse {
    @SerializedName("code")
    @Expose
    public String code;
    @SerializedName("desc")
    @Expose
    public String desc;
    @SerializedName("stsParams")
    @Expose
    public String stsParams;
}