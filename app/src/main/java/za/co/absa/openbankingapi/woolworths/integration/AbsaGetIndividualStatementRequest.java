package za.co.absa.openbankingapi.woolworths.integration;

import android.net.Uri;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public class AbsaGetIndividualStatementRequest {

    public void make(ArchivedStatement archivedStatement, final AbsaBankingOpenApiResponse.ResponseDelegate<NetworkResponse> responseDelegate) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Cookie", AbsaLoginRequest.jsessionCookie.getCookie().toString());

        Uri.Builder builder = Uri.parse(WoolworthsApplication.getAbsaBankingOpenApiServices().getBaseURL() + "/wcob/ArchivedStatementFacadeGetArchivedStatement.exp").buildUpon()
                .appendQueryParameter("documentName", archivedStatement.documentName)
                .appendQueryParameter("documentKey", archivedStatement.documentKey)
                .appendQueryParameter("extendedDocumentKey", archivedStatement.extendedDocumentKey)
                .appendQueryParameter("documentExtension", archivedStatement.documentExtension)
                .appendQueryParameter("documentWorkingDate", archivedStatement.documentWorkingDate)
                .appendQueryParameter("documentSize", archivedStatement.documentSize)
                .appendQueryParameter("documentNumberPages", archivedStatement.documentNumberPages)
                .appendQueryParameter("documentType", archivedStatement.documentType)
                .appendQueryParameter("folderName", archivedStatement.folderName)
                .appendQueryParameter("foldersEnvSuffix", archivedStatement.foldersEnvSuffix);

        String url = builder.build().toString();

        new AbsaBankingOpenApiRequest<>(url, NetworkResponse.class
                , headers, "", false, new AbsaBankingOpenApiResponse.Listener<NetworkResponse>() {

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
