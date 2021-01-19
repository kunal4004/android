package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.SecurityNotificationType;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class AbsaValidateSureCheckRequest {

    String otpToBeVerified = "";
    public AbsaValidateSureCheckRequest() {
    }

    public AbsaValidateSureCheckRequest(String otpToBeVerified) {
        this.otpToBeVerified = otpToBeVerified;
    }

    public void make(final SecurityNotificationType securityNotificationType, final AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> responseDelegate) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("action", "validateSurecheck");

        final String body = new ValidateSureCheckRequest(securityNotificationType,this.otpToBeVerified).getJson();
        new AbsaBankingOpenApiRequest<>(ValidateSureCheckResponse.class, headers, body, true, new AbsaBankingOpenApiResponse.Listener<ValidateSureCheckResponse>() {

            @Override
            public void onResponse(ValidateSureCheckResponse response, List<HttpCookie> cookies) {
                if(securityNotificationType == SecurityNotificationType.OTP){
                    responseDelegate.onSuccess(response, cookies);
                    return;
                }
                Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
                String statusCode = "0";
                try {
                    statusCode = response.getHeader().getStatusCode();
                } catch (Exception e) {
                    FirebaseManager.Companion.logException(e);
                }

                if (resultMessages == null || resultMessages.length == 0 && statusCode.equalsIgnoreCase("0"))
                    responseDelegate.onSuccess(response, cookies);
                else {
                    String resultMessage = resultMessages[0].getResponseMessage();
                    responseDelegate.onFailure(resultMessage);
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
