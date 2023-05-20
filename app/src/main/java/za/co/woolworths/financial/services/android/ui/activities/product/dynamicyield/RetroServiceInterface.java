package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.getresponse.DyKeywordSearchResponse;
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request.DyKeywordSearchRequestEvent;

public interface RetroServiceInterface {
    @POST("wfs/app/v2/collect/user/event")
    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    Call<DyKeywordSearchResponse> dynamicYieldKeywordSearch(@Body DyKeywordSearchRequestEvent event);
}
