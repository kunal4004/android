package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/22.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BankAccountType {

    @SerializedName("accountType")
    @Expose
    public String accountType;

    @SerializedName("accountTypeImage")
    @Expose
    public String accountTypeImage;

}
