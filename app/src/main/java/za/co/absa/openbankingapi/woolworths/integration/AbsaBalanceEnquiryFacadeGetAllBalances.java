package za.co.absa.openbankingapi.woolworths.integration;

import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaBalanceEnquiryRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaBalanceEnquiryResponse;
import za.co.absa.openbankingapi.woolworths.integration.dto.ErrorCodeList;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.Utils;

public class AbsaBalanceEnquiryFacadeGetAllBalances {

    public void make(String nonce, String esessionid, final AbsaBankingOpenApiResponse.ResponseDelegate<AbsaBalanceEnquiryResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString());
        Header header = new Header();
        header.setEsessionId(esessionid);
        header.setNonce(nonce);
        header.setTimestamp(Utils.getDate(0));

        String body = null;
        try {
            body = new AbsaBalanceEnquiryRequest(header).getJson();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/BalanceEnquiryFacadeGetAllBalances.exp", AbsaBalanceEnquiryResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<AbsaBalanceEnquiryResponse>() {
            @Override
            public void onResponse(AbsaBalanceEnquiryResponse response, List<HttpCookie> cookies) {
                if (response.accountList != null && response.accountList.size() > 0)
                    responseDelegate.onSuccess(response, cookies);
                else {
                    String errorDescription = ErrorCodeList.Companion.checkResult(response.header.getStatusCode());
                    if (TextUtils.isEmpty(errorDescription))
                        responseDelegate.onFailure(response.header.getResultMessages()[0].getResponseMessage());
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
