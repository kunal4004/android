package za.co.woolworths.financial.services.android.contracts;

public interface RequestListener<T> {
    void onSuccess(T response);
    void onFailure(Throwable error);
}
