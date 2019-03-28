package za.co.woolworths.financial.services.android.util;

import android.util.Log;

import com.awfs.coordination.BuildConfig;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by eesajacobs on 2018/03/01.
 */

public class WFirebaseInstanceIDService extends FirebaseInstanceIdService {
	private static final String TAG = WFirebaseInstanceIDService.class.getSimpleName();

	@Override
	public void onTokenRefresh() {
		super.onTokenRefresh();

		String token = FirebaseInstanceId.getInstance().getToken();
		NotificationUtils.getInstance().sendRegistrationToServer(token);

		String topic = "all_"+ BuildConfig.ENV.toLowerCase();
		FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

}
