package za.co.absa.openbankingapi.woolworths.integration.service;

public interface IAbsaBankingOpenApiResponseListener<T> {

	void onSuccess(T response);
	void onFailure(String errorMessage);
}
