package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class AbsaBalanceEnquiryRequest {

    @SerializedName("header")
    private Header header;

    public AbsaBalanceEnquiryRequest(Header header) {
        this.header = header;
        this.header.setOperation("GetArchivedStatementList");
        this.header.setService("ArchivedStatementFacade");
    }

    public final String getJson() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }

}
