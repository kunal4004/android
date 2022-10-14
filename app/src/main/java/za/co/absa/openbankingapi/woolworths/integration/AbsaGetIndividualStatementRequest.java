package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.NetworkResponse;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement;
import za.co.absa.openbankingapi.woolworths.integration.dto.IndividualStatementRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;

public class AbsaGetIndividualStatementRequest {

    public void make(ArchivedStatement archivedStatement, final AbsaBankingOpenApiResponse.ResponseDelegate<NetworkResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString());

        String body = null;
        try{
            body = new IndividualStatementRequest(archivedStatement).getUrlEncodedFormData();
        } catch (UnsupportedEncodingException e) {
            FirebaseManager.Companion.logException(e);
        }
        new AbsaBankingOpenApiRequest<>(AppConfigSingleton.INSTANCE.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/ArchivedStatementFacadeGetArchivedStatement.exp", NetworkResponse.class, headers, body, true, responseDelegate::onSuccess, responseDelegate::onFatalError);
    }
}
