package za.co.woolworths.financial.services.android.contracts;

public interface IResponseListener<T> {
    void onSuccess(T response);
    void onFailure(Throwable error);
}
