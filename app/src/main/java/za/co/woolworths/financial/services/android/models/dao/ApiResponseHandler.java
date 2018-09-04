package za.co.woolworths.financial.services.android.models.dao;

import za.co.woolworths.financial.services.android.util.HttpAsyncTask;

public interface ApiResponseHandler {

	void success(Object responseObject);
	void failure(String errorMessage, HttpAsyncTask.HttpErrorCode httpErrorCode);
}
