package za.co.woolworths.financial.services.android.models.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import za.co.woolworths.financial.services.android.contracts.RequestListener;

public class CompletionHandler<T> implements Callback<T> {

    protected RequestListener<T> listener;

    public CompletionHandler(RequestListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful())
            this.listener.onSuccess(response.body());
        else
            this.listener.onFailure(new Throwable());
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        this.listener.onFailure(t);
    }
}
