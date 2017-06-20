package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;

import com.awfs.coordination.R;

import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.activities.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;

/**
 * Created by eesajacobs on 2016/11/30.
 */

public class ScreenManager {

	public static void presentMain(Activity activity) {

		Intent intent = new Intent(activity, WOneAppBaseActivity.class);
		activity.startActivity(intent);
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
			SessionDao sessionDao = new SessionDao(activity, SessionDao.KEY.USER_TOKEN).get();
			params.put("id_token_hint", sessionDao.value);
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

	public static void presentSSOChangePassword(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.CHANGE_PASSWORD.rawValue());

		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}
}
