package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.HashMap;

import kotlin.Pair;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState;
import za.co.woolworths.financial.services.android.ui.activities.BiometricsWalkthrough;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl;
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatDetailActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.shop.ShoppingListDetailActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.shop.ShoppingListSearchResultActivity;
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType;

import static za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.APP_SCREEN;
import static za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.FEATURE_NAME;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.OPEN_CART_REQUEST;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE;

/**
 * Created by eesajacobs on 2016/11/30.
 */

public class ScreenManager {

	public static final int CART_LAUNCH_VALUE = 1442;
	public static final int BIOMETRICS_LAUNCH_VALUE = 1983;
	public static final int SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE = 2330;

	public static void presentMain(Activity activity, String notificationUtils) {

		Intent intent = new Intent(activity, BottomNavigationActivity.class);
		intent.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, notificationUtils);
		activity.startActivityForResult(intent, 0);
		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		activity.finish();
	}

	public static void presentMain(Activity activity, String notificationUtils, Uri data) {

		Intent intent = new Intent(activity, BottomNavigationActivity.class);
		intent.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, notificationUtils);
		intent.setData(data);
		activity.startActivityForResult(intent, 0);
		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
		activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	public static void presentSSOUpdatePassword(Activity activity) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.UPDATE_PASSWORD.rawValue());
		activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
		activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	public static void presentBiometricWalkthrough(final Activity activity){
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (!AppInstanceObject.get().isBiometricWalkthroughPresented() &&
						AuthenticateUtils.getInstance(activity).isAppSupportsAuthentication() && !AuthenticateUtils.getInstance(activity).isAuthenticationEnabled()) {
					activity.startActivityForResult(new Intent(activity, BiometricsWalkthrough.class), BIOMETRICS_LAUNCH_VALUE);
				}
			}
		}).start();

	}

	public static void presentSSOSignin(Activity activity,int requestCode) {
		Intent intent = new Intent(activity, SSOActivity.class);
		intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
		intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
		intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());
		activity.startActivityForResult(intent, requestCode);
		activity.overridePendingTransition(0, 0);
	}

	public static void presentProductDetails(Activity activity, Bundle bundle)
	{
		Intent intent = new Intent(activity, ProductDetailsActivity.class);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent,PDP_REQUEST_CODE);
		activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}

	public static void presentPayMyAccountActivity(Activity activity, Pair<? extends ApplyNowState, ? extends Account> mAccountPair) {
		Intent howToPayIntent = new Intent(activity, PayMyAccountActivity.class);
		howToPayIntent.putExtra(PayMyAccountPresenterImpl.ACCOUNT_INFO, Utils.objectToJson(mAccountPair));
		activity.startActivityForResult(howToPayIntent, PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE);
		activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}


	public static void presentPayMyAccountActivity(Activity activity, String account, String paymentMethod,PayMyAccountStartDestinationType payMyAccountStartDestinationType) {
		Intent howToPayIntent = new Intent(activity, PayMyAccountActivity.class);
		howToPayIntent.putExtra(PayMyAccountPresenterImpl.ACCOUNT_INFO, account);
		howToPayIntent.putExtra(PayMyAccountPresenterImpl.PAYMENT_METHOD, paymentMethod);
		howToPayIntent.putExtra(PayMyAccountPresenterImpl.SCREEN_TYPE, payMyAccountStartDestinationType);
		activity.startActivityForResult(howToPayIntent, PayMyAccountActivity.PAY_MY_ACCOUNT_REQUEST_CODE);
		activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}


	public static void presentShoppingCart(Activity activity){
		Intent openCartActivity = new Intent(activity, CartActivity.class);
		activity.startActivityForResult(openCartActivity, OPEN_CART_REQUEST);
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	public static void presentShoppingListDetailActivity(Activity activity, String listId, String listName) {
		Intent openShoppingListDetailActivity = new Intent(activity, ShoppingListDetailActivity.class);
		openShoppingListDetailActivity.putExtra("listId", listId);
		openShoppingListDetailActivity.putExtra("listName", listName);
		activity.startActivityForResult(openShoppingListDetailActivity, SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE);
		activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

	}

	public static void presentShoppingListDetailActivity(Activity activity, String listId, String listName, boolean openFromMyList) {
		Intent openShoppingListDetailActivity = new Intent(activity, ShoppingListDetailActivity.class);
		openShoppingListDetailActivity.putExtra("listId", listId);
		openShoppingListDetailActivity.putExtra("listName", listName);
		openShoppingListDetailActivity.putExtra("openFromMyList",openFromMyList);
		activity.startActivityForResult(openShoppingListDetailActivity, SHOPPING_LIST_DETAIL_ACTIVITY_REQUEST_CODE);
		activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

	}

	public static void presentShoppingListSearchResult(Activity activity, String searchTerm, String listId) {
		Intent openShoppingListSearchResultActivity = new Intent(activity, ShoppingListSearchResultActivity.class);
		openShoppingListSearchResultActivity.putExtra("searchTerm", searchTerm);
		openShoppingListSearchResultActivity.putExtra("listID", listId);
		activity.startActivityForResult(openShoppingListSearchResultActivity, ShoppingListSearchResultActivity.SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE);
		activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

	}

	public static void presentProductDetails(Activity activity,String productName, ProductList productList ){
		Gson gson = new Gson();
		String strProductList = gson.toJson(productList);
		Bundle bundle = new Bundle();
		bundle.putString("strProductList", strProductList);
		bundle.putString("strProductCategory", productName);
		presentProductDetails(activity, bundle);
	}

	public static void presentWhatsAppChatToUsActivity(Activity activity, String featureName, String appScreen) {
		Intent openChatToUsWhatsAppActivity = new Intent(activity, WhatsAppChatDetailActivity.class);
		openChatToUsWhatsAppActivity.putExtra(FEATURE_NAME, featureName);
		openChatToUsWhatsAppActivity.putExtra(APP_SCREEN, appScreen);
		activity.startActivity(openChatToUsWhatsAppActivity);
		activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}
}
