package za.co.absa.openbankingapi.woolworths.integration.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StatementListResponse {

    @SerializedName("header")
    private Header header;

    @SerializedName("archivedStatementList")
    public ArrayList<ArchivedStatement> archivedStatementList;
}
