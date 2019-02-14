package za.co.absa.openbankingapi.woolworths.integration.service;

import com.android.volley.Response;

public class AbsaBankingOpenApiResponse {

	public interface Listener<T> extends Response.Listener<T> {
		void onSetCookies(String cookies);
	}
}
