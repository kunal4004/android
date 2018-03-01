package za.co.woolworths.financial.services.android.util;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by eesajacobs on 2018/03/01.
 */

public class WFirebaseInstanceIDService extends FirebaseInstanceIdService {

	@Override
	public void onTokenRefresh() {
		super.onTokenRefresh();

		String token = FirebaseInstanceId.getInstance().getToken();
		PushNotificationHandler.getInstance().sendRegistrationToServer(token);
	}
}
