package za.co.woolworths.financial.services.android.contracts;

import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;

public interface ConfigResponseListener {

    void onSuccess(ConfigResponse response);
    void onFailure(Throwable throwable);
}
