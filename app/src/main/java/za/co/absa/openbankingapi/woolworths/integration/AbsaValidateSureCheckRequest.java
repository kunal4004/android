package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dao.JSession;
import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.VolleySingleton;

public class AbsaValidateSureCheckRequest {

	private VolleySingleton requestQueue;

	public AbsaValidateSureCheckRequest(final Context context){
		this.requestQueue = VolleySingleton.getInstance(context.getApplicationContext());
	}

	public void make(final JSession jSession, final AbsaBankingOpenApiResponse.ResponseDelegate<ValidateSureCheckResponse> responseDelegate){
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		headers.put("action", "validateSurecheck");
		headers.put("JSESSIONID", jSession.getId());

		final String body = new ValidateSureCheckRequest(jSession.getId()).getJson();
		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>(ValidateSureCheckResponse.class, headers, body, new AbsaBankingOpenApiResponse.Listener<ValidateSureCheckResponse>(){

			@Override
			public void onResponse(ValidateSureCheckResponse response, List<HttpCookie> cookies) {
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				if (resultMessages == null || resultMessages.length == 0)
					responseDelegate.onSuccess(response, cookies);

				else
					responseDelegate.onFailure(resultMessages[0].getResponseMessage());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseDelegate.onFailure(error.getMessage());
			}
		});

		List<String> cookies = new ArrayList<>();
		cookies.add(jSession.getCookie().toString());
		request.setCookies(cookies);
		request.setTag(AbsaValidateSureCheckRequest.class.getSimpleName());

		requestQueue.addToRequestQueue(request,AbsaValidateSureCheckRequest.class);
	}
}
