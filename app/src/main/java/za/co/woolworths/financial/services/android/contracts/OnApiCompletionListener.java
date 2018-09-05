package za.co.woolworths.financial.services.android.contracts;

import za.co.woolworths.financial.services.android.util.HttpAsyncTask;

public interface OnApiCompletionListener<T> {
	void success(T responseObject);
	void failure(String errorMessage, HttpAsyncTask.HttpErrorCode httpErrorCode);
}
