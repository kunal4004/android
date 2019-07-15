package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import za.co.woolworths.financial.services.android.util.Utils;

public class StatementListRequest {
    @SerializedName("header")
    private Header header;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("accountNumber")
    private String accountNumber;

    public StatementListRequest(Header header, String accountNumber) {
        this.header = header;
        this.fromDate = Utils.getDate(6);
        this.toDate = Utils.getDate(0);
        this.accountNumber = accountNumber;
    }

    public final String getJson() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
