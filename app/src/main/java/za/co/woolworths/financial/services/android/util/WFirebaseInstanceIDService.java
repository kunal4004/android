package za.co.woolworths.financial.services.android.util;

import com.awfs.coordination.BuildConfig;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * Created by eesajacobs on 2018/03/01.
 */

public class WFirebaseInstanceIDService extends FirebaseMessagingService {
	private static final String TAG = WFirebaseInstanceIDService.class.getSimpleName();

	@Override
	public void onNewToken(String s) {
		super.onNewToken(s);

		String token = FirebaseInstanceId.getInstance().getToken();
		NotificationUtils.getInstance().sendRegistrationToServer(token);

		String topic = "all_"+ BuildConfig.ENV.toLowerCase();
		FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

}
