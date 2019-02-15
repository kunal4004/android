package za.co.absa.openbankingapi.woolworths.integration.service;

import java.net.HttpCookie;
import java.util.List;

public class AbsaBankingOpenApiResponse {

	public interface Listener<T>{
		void onResponse(T response, List<HttpCookie> cookies);
	}
}
