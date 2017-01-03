package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/20.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class Bank {
    @SerializedName("bankName")
    @Expose
    public String bankName;

    public Bank(String bankName) {
        this.bankName = bankName;
    }
}
