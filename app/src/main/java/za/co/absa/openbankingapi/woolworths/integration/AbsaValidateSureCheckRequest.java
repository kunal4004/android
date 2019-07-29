package za.co.absa.openbankingapi.woolworths.integration;


import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.ErrorCodeList;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;

public class AbsaValidateSureCheckRequest {


    public AbsaValidateSureCheckRequest() {
    }

    public void make(final AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> responseDelegate) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("action", "validateSurecheck");

        final String body = new ValidateSureCheckRequest().getJson();
        new AbsaBankingOpenApiRequest<>(ValidateSureCheckResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<ValidateSureCheckResponse>() {

            @Override
            public void onResponse(ValidateSureCheckResponse response, List<HttpCookie> cookies) {
                Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
                if (resultMessages == null || resultMessages.length == 0)
                    responseDelegate.onSuccess(response, cookies);
                else {
                    String errorDescription = ErrorCodeList.Companion.checkResult(response.getHeader().getStatusCode());
                    if (TextUtils.isEmpty(errorDescription))
                        responseDelegate.onFailure(resultMessages[0].getResponseMessage());
                    else
                        responseDelegate.onFailure(errorDescription);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseDelegate.onFatalError(error);
            }
        });


    }
}
