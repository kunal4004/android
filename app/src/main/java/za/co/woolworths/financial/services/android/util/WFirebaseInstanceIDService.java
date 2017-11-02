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
        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);

    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        Log.d(TAG, "sendRegistrationToServer: " + token);
        final CreateUpdateDevice device=new CreateUpdateDevice();
        device.appInstanceId= Utils.getUniqueDeviceID(getApplicationContext());
        device.pushNotificationToken=token;

        //Don't update token if pushNotificationToken or appInstanceID NULL
        if(device.appInstanceId == null || device.pushNotificationToken==null)
            return;

        //Sending Token and app instance Id to App server
        //Need to be done after Login

        new HttpAsyncTask<String, String, CreateUpdateDeviceResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected CreateUpdateDeviceResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getResponseOnCreateUpdateDevice(device);
            }

            @Override
            protected Class<CreateUpdateDeviceResponse> httpDoInBackgroundReturnType() {
                return CreateUpdateDeviceResponse.class;
            }

            @Override
            protected CreateUpdateDeviceResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                CreateUpdateDeviceResponse createUpdateResponse = new CreateUpdateDeviceResponse();
                createUpdateResponse.response = new Response();
                return createUpdateResponse;
            }

            @Override
            protected void onPostExecute(CreateUpdateDeviceResponse createUpdateResponse) {
                super.onPostExecute(createUpdateResponse);


            }
        }.execute();

    }

}

