package za.co.absa.openbankingapi.woolworths.integration;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.AbsaIndividualStatementResponse;
import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class AbsaGetIndividualStatementRequest {

    public void make(ArchivedStatement archivedStatement, final AbsaBankingOpenApiResponse.ResponseDelegate<AbsaIndividualStatementResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString());

        String url = WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/ArchivedStatementFacadeGetArchivedStatement.exp" + "?documentName=" + "MISS+S+SIMONS" + "&documentKey=" + archivedStatement.documentKey + "&extendedDocumentKey=" + archivedStatement.extendedDocumentKey + "&documentExtension=" + archivedStatement.documentExtension + "&documentWorkingDate=" + archivedStatement.documentWorkingDate + "&documentSize=" + archivedStatement.documentSize + "&documentNumberPages=" + archivedStatement.documentNumberPages + "&documentType=" + archivedStatement.documentType + "&folderName=" + archivedStatement.folderName + "&foldersEnvSuffix=" + archivedStatement.foldersEnvSuffix;

        new AbsaBankingOpenApiRequest<>(url, AbsaIndividualStatementResponse.class
                , headers, "", true, new AbsaBankingOpenApiResponse.Listener<AbsaIndividualStatementResponse>() {

            @Override
            public void onResponse(AbsaIndividualStatementResponse response, List<HttpCookie> cookies) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseDelegate.onFatalError(error);
            }
        });
    }
}
