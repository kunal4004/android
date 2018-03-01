package za.co.woolworths.financial.services.android.util;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;

/**
 * Created by W7099877 on 09/11/2016.
 */

public class WFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = WFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        PushNotificationHandler.getInstance().sendRegistrationToServer(refreshedToken);
    }
}

