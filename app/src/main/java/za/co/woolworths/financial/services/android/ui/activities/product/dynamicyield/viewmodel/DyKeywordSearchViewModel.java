package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import za.co.woolworths.financial.services.android.models.network.ApiInterface;
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderImpl;
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderInterface;
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.RetroInstance;
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.RetroServiceInterface;
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.getresponse.DyKeywordSearchResponse;
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request.DyKeywordSearchRequestEvent;

public class DyKeywordSearchViewModel extends ViewModel {

    private MutableLiveData<DyKeywordSearchResponse> createDyKeywordSearchLiveData;
    public DyKeywordSearchViewModel() {
        createDyKeywordSearchLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<DyKeywordSearchResponse> getCreateKeywordSearchObserver() {
        return createDyKeywordSearchLiveData;
    }

    public void createKeywordSearch(DyKeywordSearchRequestEvent dyKeywordSearchRequestEvent) {
        RetroServiceInterface retroServiceInterface = RetroInstance.getRetroInstance().create(RetroServiceInterface.class);
        Call<DyKeywordSearchResponse> call = retroServiceInterface.dynamicYieldKeywordSearch(dyKeywordSearchRequestEvent);
        call.enqueue(new Callback<DyKeywordSearchResponse>() {
            @Override
            public void onResponse(Call<DyKeywordSearchResponse> call, Response<DyKeywordSearchResponse> response) {
                if (response.isSuccessful()) {
                    createDyKeywordSearchLiveData.postValue(response.body());
                }else {
                    createDyKeywordSearchLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<DyKeywordSearchResponse> call, Throwable t) {
                createDyKeywordSearchLiveData.postValue(null);
            }
        });
    }
}
