package za.co.woolworths.financial.services.android.util;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import za.co.woolworths.financial.services.android.models.WfsApi;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;

/**
 * Created by eesajacobs on 2018/03/01.
 */

public class PushNotificationHandler {
	private static PushNotificationHandler instance;

	public static PushNotificationHandler getInstance() {
		if (instance == null)
			instance = new PushNotificationHandler();

		return instance;
	}

	public void sendRegistrationToServer(String token) {

		// sending gcm token to server
		final CreateUpdateDevice device = new CreateUpdateDevice();
		device.appInstanceId = Utils.getUniqueDeviceID(WoolworthsApplication.getInstance().getApplicationContext());

		if (token == null)
			device.pushNotificationToken = FirebaseInstanceId.getInstance(FirebaseApp.getInstance()).getToken();
		else
			device.pushNotificationToken = token;

		//Don't update token if pushNotificationToken or appInstanceID NULL
		if(device.appInstanceId == null || device.pushNotificationToken==null)
			return;

		Log.d("PushNotificationHandler", " Token: " + device.pushNotificationToken);

		//Sending Token and app instance Id to App server
		//Need to be done after Login

		new HttpAsyncTask<String, String, CreateUpdateDeviceResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected CreateUpdateDeviceResponse httpDoInBackground(String... params) {
				WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
				WfsApi api = woolworthsApplication.getApi();
				return api.getResponseOnCreateUpdateDevice(device);
			}

			@Override
			protected Class<CreateUpdateDeviceResponse> httpDoInBackgroundReturnType() {
				return CreateUpdateDeviceResponse.class;
			}

			@Override
			protected CreateUpdateDeviceResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				CreateUpdateDeviceResponse createUpdateResponse = new CreateUpdateDeviceResponse();
				createUpdateResponse.response = new Response();

				Log.d("PushNotificationHandler", "Error: " + createUpdateResponse.response.desc);


				return createUpdateResponse;
			}

			@Override
			protected void onPostExecute(CreateUpdateDeviceResponse createUpdateResponse) {
				super.onPostExecute(createUpdateResponse);
			}
		}.execute();
	}
}
