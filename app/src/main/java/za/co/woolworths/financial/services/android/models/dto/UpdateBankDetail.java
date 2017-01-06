package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/22.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateBankDetail {

    @SerializedName("cliOfferID")
    @Expose
    private Integer cliOfferID;
    @SerializedName("bankName")
    @Expose
    private String bankName;
    @SerializedName("accountType")
    @Expose
    private String accountType;
    @SerializedName("accountNumber")
    @Expose
    private String accountNumber;

    public Integer getCliOfferID() {
        return cliOfferID;
    }

    public void setCliOfferID(Integer cliOfferID) {
        this.cliOfferID = cliOfferID;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}