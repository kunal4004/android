package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement;
import za.co.absa.openbankingapi.woolworths.integration.dto.IndividualStatementRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class AbsaGetIndividualStatementRequest {

    public void make(ArchivedStatement archivedStatement, final AbsaBankingOpenApiResponse.ResponseDelegate<NetworkResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString());

        String body = null;
        try{
            body = new IndividualStatementRequest(archivedStatement).getUrlEncodedFormData();
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }
        new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/ArchivedStatementFacadeGetArchivedStatement.exp", NetworkResponse.class
                , headers, body, true, new AbsaBankingOpenApiResponse.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response, List<HttpCookie> cookies) {
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
