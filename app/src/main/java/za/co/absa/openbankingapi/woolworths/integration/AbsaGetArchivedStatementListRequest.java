package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

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

public class AbsaGetArchivedStatementListRequest {

    public void make(Header header, String accountNumber, final AbsaBankingOpenApiResponse.ResponseDelegate<StatementListResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString() + ";" + AbsaLoginRequest.xfpt.getCookie().toString() + ";" + AbsaLoginRequest.wfpt.getCookie().toString());

        String body = null;
        try {
            body = new StatementListRequest(header, accountNumber).getJson();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/BalanceEnquiryFacadeGetAllBalances.exp", StatementListResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<StatementListResponse>() {
            @Override
            public void onResponse(StatementListResponse response, List<HttpCookie> cookies) {
                responseDelegate.onSuccess(response, cookies);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseDelegate.onFatalError(error);
            }
        });
    }
}
