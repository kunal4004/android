package za.co.absa.openbankingapi.woolworths.integration;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.StatementListRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.StatementListResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class AbsaGetArchivedStatementListRequest {

    public void make(Header header, String accountNumber, final AbsaBankingOpenApiResponse.ResponseDelegate<StatementListResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString() + ";" + AbsaLoginRequest.xfpt.getCookie().toString() + ";" + AbsaLoginRequest.wfpt.getCookie().toString());

        header.setService("ArchivedStatementFacade");
        header.setOperation("GetArchivedStatementList");

        String body = null;
        try {
            body = new StatementListRequest(header, accountNumber).getJson();
        } catch (Exception e) {
            FirebaseManager.Companion.logException(e);
        }

        new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/ArchivedStatementFacadeGetArchivedStatementList.exp", StatementListResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<StatementListResponse>() {
            @Override
            public void onResponse(StatementListResponse response, List<HttpCookie> cookies) {
                responseDelegate.onSuccess(response, cookies);
            }
        }, error -> responseDelegate.onFatalError(error));
    }
}
