package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/22.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BankAccountTypes {

    @SerializedName("bankAccountTypes")
    @Expose
    public List<BankAccountType> bankAccountTypes = null;
    @SerializedName("response")
    @Expose
    public BankAccountResponse response;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;

}
