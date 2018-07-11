package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.awfs.coordination.R;

import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.ui.activities.BiometricsWalkthrough;
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity;
import za.co.woolworths.financial.services.android.ui.activities.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;

/**
 * Created by eesajacobs on 2016/11/30.
 */

public class ScreenManager {

	public static final int CART_LAUNCH_VALUE = 1442;

	public static void presentMain(Activity activity, String notificationUtils) {

		Intent intent = new Intent(activity, BottomNavigationActivity.class);
		intent.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, notificationUtils);
		activity.startActivityForResult(intent, 0);
		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		activity.finish();
	}

	public static void presentSSOSignin(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(0, 0);
	}

	public static void presentCartSSOSignin(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());
		activity.startActivityForResult(intent, CART_LAUNCH_VALUE);
		activity.overridePendingTransition(0, 0);
	}

	public static void presentExpiredTokenSSOSignIn(Activity activity, String stsParams) {
		WoolworthsApplication woolworthsApplication = (WoolworthsApplication) activity
				.getApplication();
		SessionUtilities.getInstance().setSTSParameters(stsParams);

		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());
		intent.putExtra(SSOActivity.TAG_SCOPE, stsParams);
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(0, 0);
	}

	public static void presentSSORegister(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.REGISTER.rawValue());
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(0, 0);
	}

	public static void presentOnboarding(Activity activity) {

		Intent intent = new Intent(activity, OnBoardingActivity.class);

		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		activity.finish();
	}

	public static void presentSSOLinkAccounts(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());
		intent.putExtra(SSOActivity.TAG_SCOPE, "C2Id");
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(0, 0);
	}

	public static void presentSSOLogout(Activity activity) {
		HashMap<String, String> params = new HashMap<String, String>();
		try {
			params.put("id_token_hint", SessionUtilities.getInstance().getSessionToken());
			params.put("post_logout_redirect_uri", WoolworthsApplication.getSsoRedirectURILogout());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.LOGOUT.rawValue());
		intent.putExtra(SSOActivity.TAG_EXTRA_QUERYSTRING_PARAMS, params);

		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(0, 0);
	}

	public static void presentSSOUpdateProfile(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.UPDATE_PROFILE.rawValue());
		Log.e("updateDetail_PROFILE", SSOActivity.Path.UPDATE_PROFILE.rawValue());
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	public static void presentSSOUpdatePassword(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.UPDATE_PASSWORD.rawValue());
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	public static void presentBiometricWalkthrough(Activity activity){
		if (!AppInstanceObject.get().isBiometricWalkthroughPresented() &&
				AuthenticateUtils.getInstance(activity).isAppSupportsAuthentication() && !AuthenticateUtils.getInstance(activity).isAuthenticationEnabled()) {
			activity.startActivity(new Intent(activity, BiometricsWalkthrough.class));
		}
	}
}
