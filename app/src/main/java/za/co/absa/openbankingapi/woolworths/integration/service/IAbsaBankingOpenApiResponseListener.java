package za.co.absa.openbankingapi.woolworths.integration.service;

import java.net.HttpCookie;
import java.util.List;

public interface IAbsaBankingOpenApiResponseListener<T> {

	void onSuccess(T response, List<HttpCookie> cookies);
	void onFailure(String errorMessage);
}
