package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AbsaBalanceEnquiryResponse {

    @SerializedName("header")
    public Header header;

    @SerializedName("accountList")
    public ArrayList<AccountListItem> accountList;
}
