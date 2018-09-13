package za.co.woolworths.financial.services.android.util;

public interface OnEventListener<T> {
	 void onSuccess(T object);
	 void onFailure(String e);
}