package za.co.absa.openbankingapi.woolworths.integration.service;

public class AbsaBankingOpenApiResponse {

	public interface Listener<T>{
		void onResponse(T response, String cookies);
	}
}
