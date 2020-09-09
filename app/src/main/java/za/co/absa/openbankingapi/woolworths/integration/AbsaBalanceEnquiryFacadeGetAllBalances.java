package za.co.absa.openbankingapi.woolworths.integration;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaBalanceEnquiryRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaBalanceEnquiryResponse;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class AbsaBalanceEnquiryFacadeGetAllBalances {

    public void make(String nonce, String esessionid, String timestampString, final AbsaBankingOpenApiResponse.ResponseDelegate<AbsaBalanceEnquiryResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString());
        Header header = new Header();
        header.setService("BalanceEnquiryFacade");
        header.setOperation("GetAllBalances");
        header.setChannel("I");
        header.setLanguage("en");
        header.setOrganization("WCOBMOBAPP");
        header.setBrand("WCOBMOBAPP");
        header.setEsessionId(esessionid);
        header.setNonce(nonce);
        header.setTimestamp(timestampString);

        String body = null;
        try {
            body = new AbsaBalanceEnquiryRequest(header).getJson();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        new AbsaBankingOpenApiRequest<>(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/BalanceEnquiryFacadeGetAllBalances.exp", AbsaBalanceEnquiryResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<AbsaBalanceEnquiryResponse>() {
            @Override
            public void onResponse(AbsaBalanceEnquiryResponse response, List<HttpCookie> cookies) {

                String statusCode = "0";
                try {
                    statusCode = response.header.getStatusCode();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }

                if (response.accountList != null && response.accountList.size() > 0 && statusCode.equalsIgnoreCase("0"))
                    responseDelegate.onSuccess(response, cookies);
                else {
                    responseDelegate.onFailure(response.header.getResultMessages()[0].getResponseMessage());
                }
            }
        }, error -> responseDelegate.onFatalError(error));
    }
}
