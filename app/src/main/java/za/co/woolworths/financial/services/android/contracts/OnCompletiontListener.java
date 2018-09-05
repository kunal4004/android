package za.co.woolworths.financial.services.android.contracts;

public interface OnCompletiontListener<T> {
	void success(T object);
	void failure(String errorMessage);
}
