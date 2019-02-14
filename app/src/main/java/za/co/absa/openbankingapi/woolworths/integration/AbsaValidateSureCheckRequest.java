package za.co.absa.openbankingapi.woolworths.integration;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.absa.openbankingapi.woolworths.integration.dto.Header;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckRequest;
import za.co.absa.openbankingapi.woolworths.integration.dto.ValidateSureCheckResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiRequest;
import za.co.absa.openbankingapi.woolworths.integration.service.AbsaBankingOpenApiResponse;
import za.co.absa.openbankingapi.woolworths.integration.service.IAbsaBankingOpenApiResponseListener;

public class AbsaValidateSureCheckRequest {

	private RequestQueue requestQueue;

	public AbsaValidateSureCheckRequest(final Context context){

		this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
	}

	public void make(final String jSessionId, final IAbsaBankingOpenApiResponseListener<ValidateSureCheckResponse> responseListener){
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("action", "validateSurecheck");
		headers.put("JSESSIONID", jSessionId);

		final String body = new ValidateSureCheckRequest(jSessionId).getJson();
		final AbsaBankingOpenApiRequest request = new AbsaBankingOpenApiRequest<>(ValidateSureCheckResponse.class, headers, body, new AbsaBankingOpenApiResponse.Listener<ValidateSureCheckResponse>(){

			@Override
			public void onSetCookies(String cookies) {

			}

			@Override
			public void onResponse(ValidateSureCheckResponse response) {
				Header.ResultMessage[] resultMessages = response.getHeader().getResultMessages();
				if (resultMessages == null || resultMessages.length == 0)
					responseListener.onSuccess(response);

				else
					responseListener.onFailure(resultMessages[0].getResponseMessage());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				responseListener.onFailure(error.getMessage());
			}
		});

		List<String> cookies = new ArrayList<>();
		cookies.add("JSESSIONID=0000" + jSessionId + ":19maojp8e");
		request.setCookies(cookies);

		requestQueue.add(request);
	}
}
