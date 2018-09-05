package za.co.woolworths.financial.services.android.contracts;

public interface OnResultListener<T> extends OnCompletionListener {

	void success(T object);
	void failure(String errorMessage);
}
