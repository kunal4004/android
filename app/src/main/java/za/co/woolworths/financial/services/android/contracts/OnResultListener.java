package za.co.woolworths.financial.services.android.contracts;

import za.co.woolworths.financial.services.android.util.HttpAsyncTask;

public interface OnResultListener<T> extends OnCompletionListener {

	void success(T object);
	void failure(String errorMessage, HttpAsyncTask.HttpErrorCode httpErrorCode);
}
