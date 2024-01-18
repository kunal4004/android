package za.co.woolworths.financial.services.android.ui.activities;

import static za.co.woolworths.financial.services.android.util.Utils.DY_CHANNEL;
import static za.co.woolworths.financial.services.android.util.Utils.IDENTIFY;
import static za.co.woolworths.financial.services.android.util.Utils.IDENTIFY_V1;
import static za.co.woolworths.financial.services.android.util.Utils.LOGIN;
import static za.co.woolworths.financial.services.android.util.Utils.LOGIN_V1;
import static za.co.woolworths.financial.services.android.util.Utils.SIGNUP;
import static za.co.woolworths.financial.services.android.util.Utils.SIGNUP_V1;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.AppConfigSingleton;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl;
import za.co.woolworths.financial.services.android.models.network.NetworkConfig;
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant;
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Options;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Page;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyHomePageViewModel;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel;
import za.co.woolworths.financial.services.android.ui.wfs.common.NetworkUtilsKt;
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.SSORequiredParameter;
import za.co.woolworths.financial.services.android.util.ServiceTools;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;
import za.co.woolworths.financial.services.android.util.wenum.ConfirmLocation;

@AndroidEntryPoint
public class SSOActivity extends WebViewActivity {

	public ErrorHandlerView mErrorHandlerView;
	private WGlobalState mGlobalState;
	private boolean isKMSIChecked;
	private boolean isExtractFormDataCompleted;
	public enum SSOActivityResult {
		LAUNCH(1),
		NO_CACHED_STATE(2),
		NO_CACHED_NONCE(3),
		STATE_MISMATCH(4),
		NONCE_MISMATCH(5),
		SUCCESS(6),
		SIGNED_OUT(8),
		CHANGE_PASSWORD(9),
		FORGOT_PASSWORD(10);
		private int result;

		SSOActivityResult(int i) {
			this.result = i;
		}

		public int rawValue() {
			return result;
		}

	}

	public static final String TAG = "SSOActivity";
	public static final String TAG_PROTOCOL = "TAG_PROTOCOL";
	public static final String TAG_HOST = "TAG_HOST";
	public static final String TAG_PATH = "TAG_PATH";
	public static final String TAG_JWT = "TAG_JWT";
	public static final String TAG_SCOPE = "TAG_SCOPE";
	public static final String TAG_PASSWORD = "TAG_PASSWORD";
	public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
	public static final String FORGOT_PASSWORD_VALUE = "PASSWORD";
	public static final String IS_USER_BROWSING = "IS_USER_BROWSING";
	private String forgotPasswordLogin = "login=true&source=oneapp";
	private String TNC_TITLE = "Woolworths.co.za";

	@Inject
	NotificationUtils notificationUtils;

	public static final String TAG_EXTRA_QUERYSTRING_PARAMS = "TAG_EXTRA_QUERYSTRING_PARAMS";
	//Default redirect url used by LOGIN AND LINK CARDS
	private static String redirectURIString = AppConfigSingleton.INSTANCE.getSsoRedirectURI();
	private Protocol protocol;
	private Boolean isUserBrowsing = false;
	private Host host;
	public Path path;
	private Map<String, String> extraQueryStringParams;

	private String state;
	private final String nonce;
	private String stsParams;
	private String forgotPassword;
	private String dyServerId = null;
	private String dySessionId = null;
	public static String IPAddress = NetworkUtilsKt.getIpAddress(WoolworthsApplication.getInstance(),WoolworthsApplication.getInstance());
	private String jwt = null;
	private DyChangeAttributeViewModel dyReportEventViewModel;
	private NetworkConfig config;
	private DyHomePageViewModel dyHomePageViewModel;

	public SSOActivity() {
		this.state = UUID.randomUUID().toString();
		this.nonce = UUID.randomUUID().toString();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.instantiateWebView();
		Utils.updateStatusBarBackground(this);
		mGlobalState = ((WoolworthsApplication) getApplication()).getWGlobalState();
		mErrorHandlerView = new ErrorHandlerView(SSOActivity.this, (RelativeLayout) findViewById
				(R.id.no_connection_layout));
		isKMSIChecked = Utils.getUserKMSIState();
		if (isKMSIChecked){
			KotlinUtils.Companion.convertActivityToTranslucent(this);
		}
		handleUIForKMSIEntry((Utils.getUserKMSIState() && SSOActivity.this.path == Path.SIGNIN));
		showProfileProgressBar();
		config = new NetworkConfig(new AppContextProviderImpl());
		dyReportEventViewModel = new ViewModelProvider(this).get(DyChangeAttributeViewModel.class);
		dyHomePageViewModel = new ViewModelProvider(this).get(DyHomePageViewModel.class);
	}

	// Display progress bar as soon as user land on profile
	private void showProfileProgressBar() {
		if (SSOActivity.this.path!=null && SSOActivity.this.path==Path.UPDATE_PROFILE
				&& NetworkManager.getInstance().isConnectedToNetwork(this)){
			showProgressBar();
		}
	}

	private void instantiateWebView() {
		this.webView.setWebViewClient(this.webviewClient);
		this.webView.getSettings().setUseWideViewPort(true);
		this.webView.getSettings().setLoadWithOverviewMode(true);
		this.webView.getSettings().setDomStorageEnabled(true);
		this.webView.addJavascriptInterface(new KMSIState(),"injection");
		webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		this.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		this.webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);

				// ensure variables are not null
				title = TextUtils.isEmpty(title) ? "" : title;
				redirectURIString = TextUtils.isEmpty(redirectURIString) ? "" : redirectURIString;
				SSOActivity.this.state = TextUtils.isEmpty(SSOActivity.this.state) ? "" : SSOActivity.this.state;
				final String urlStateComponent = "?state=".concat(SSOActivity.this.state).toLowerCase();

				ArrayList<String> invalidTitles = new ArrayList<>(
						Arrays.asList("about:blank".toLowerCase(),
								getString(R.string.sso_title_text_submit_this_form).toLowerCase(),
								redirectURIString.toLowerCase(),
								redirectURIString.concat(urlStateComponent))
				);

				if (invalidTitles.contains(title.toLowerCase()) || title.toLowerCase().endsWith(urlStateComponent) || SSOActivity.this.path == Path.FORGOT_PASSWORD) {
					toolbarTextView.setText("");
				} else
					toolbarTextView.setText(title);

				if(title.contains(forgotPasswordLogin)){
					Intent i = new Intent(SSOActivity.this, BottomNavigationActivity.class);
					i.putExtra(FORGOT_PASSWORD,FORGOT_PASSWORD_VALUE);
					startActivity(i);
					finish();
					overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
				}
			}
		});
		retryConnect();
	}

	private void retryConnect() {
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(SSOActivity.this)) {
					WebBackForwardList history = webView.copyBackForwardList();
					int index = -1;
					String url;
					while (webView.canGoBackOrForward(index)) {
						if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
							webView.goBackOrForward(index);
							url = history.getItemAtIndex(-index).getUrl();
							break;
						}
						index--;
					}
					mErrorHandlerView.hideErrorHandlerLayout();
					handleUIForKMSIEntry(Utils.getUserKMSIState());
				}
			}
		});
	}

	//override intent to return expected link that's to be used in the WebViewActivity
	@Override
	public synchronized Intent getIntent() {
		Intent intent = super.getIntent();
		Bundle bundle = intent.getExtras();

		this.protocol = Protocol.getProtocolByRawValue(bundle.getString(SSOActivity.TAG_PROTOCOL));
		this.host = Host.getHostByRawValue(bundle.getString(SSOActivity.TAG_HOST));
		this.path = Path.getPathByRawValue(bundle.getString(SSOActivity.TAG_PATH));
		this.isUserBrowsing = bundle.getBoolean(IS_USER_BROWSING, false);
		this.extraQueryStringParams = (Map<String, String>) intent.getSerializableExtra(SSOActivity.TAG_EXTRA_QUERYSTRING_PARAMS);

		String scope = bundle.getString(SSOActivity.TAG_SCOPE);
		forgotPassword = bundle.getString(SSOActivity.TAG_PASSWORD);
		String link = this.constructAndGetAuthorisationRequestURL(scope);

		bundle.putString("title", "SIGN IN");
		bundle.putString("link", link);
		intent.putExtra("Bundle", bundle);

		return intent;
	}

	public enum Protocol implements SSORequiredParameter {
		HTTP("http"),
		HTTPS("https");

		private final String protocol;

		Protocol(String protocol) {
			this.protocol = protocol;
		}

		public String rawValue() {
			return protocol;
		}

		public static Protocol getProtocolByRawValue(String rawValue) {
			for (Protocol p : Protocol.values()) {
				if (p != null
						&& p.rawValue() != null
						&& !TextUtils.isEmpty(rawValue)
						&& rawValue.equalsIgnoreCase(p.rawValue()))
					return p;
			}
			return null;
		}
	}

	public enum Host implements SSORequiredParameter {
		STS(AppConfigSingleton.INSTANCE.getStsURI());

		private final String host;

		Host(String protocol) {
			this.host = protocol;
		}

		@Override
		public String rawValue() {
			return host;
		}

		public static Host getHostByRawValue(String rawValue) {
			for (Host h : Host.values()) {
				if (h != null
						&& h.rawValue() != null
						&& !TextUtils.isEmpty(rawValue)
						&& rawValue.equalsIgnoreCase(h.rawValue()))
					return h;
			}
			return null;
		}
	}

	public enum Path implements SSORequiredParameter {
		SIGNIN("customerid/connect/authorize"),
		REGISTER("customerid/register/step1"),
		LOGOUT("customerid/connect/endsession"),
		UPDATE_PASSWORD("customerid/userdetails/password"),
		UPDATE_PROFILE("customerid/userdetails"),
		FORGOT_PASSWORD("forgot-password");

		private final String path;

		Path(String protocol) {
			this.path = protocol;
		}

		public String rawValue() {
			return this.path;
		}

		public static Path getPathByRawValue(String rawValue) {
			for (Path p : Path.values()) {
				if (p != null
						&& p.rawValue() != null
						&& !TextUtils.isEmpty(rawValue)
						&& rawValue.equalsIgnoreCase(p.rawValue()))
					return p;
			}
			return null;
		}
	}

	private String constructAndGetAuthorisationRequestURL(String scope) {
		if (this.path == null) return "";
		switch (this.path) {

			case SIGNIN:
				redirectURIString = AppConfigSingleton.INSTANCE.getSsoRedirectURI();

                /*
				* // Check if sts params were supplied.
      guard let query = stsParams else {
        break
      }

      // New array to hold the STS query items.
      var stsQueryItems = [URLQueryItem]()

      // Extract query items.
      let queryItemArray = createQueryItems(query: query)

      // Loop over query items.
      for queryItem in queryItemArray {

        // Check if the "scopes" query parameter was provided.
        if queryItem.name == baseScopeItem.name {
          // Amend the "base: scope query item.

          // Update the parameters of the "scope" query item to include the new triggering call's scope parameters (e.g. C2ID).
          let newScopeValue = baseScopeItem.value! + " " + queryItem.value!
          let updatedBaseScopeItem = URLQueryItem(name: baseScopeItem.name, value: newScopeValue)
          if debugMode { print("[vc_sso] STS scope item updated: \(updatedBaseScopeItem)") }
          stsQueryItems.append(updatedBaseScopeItem)

        } else {
          // Append all other query item. E.g. max_age
          stsQueryItems.append(queryItem)
          if debugMode { print("[vc_sso] STS query item added: \(queryItem)") }
        }

      }

      return stsQueryItems // Return the updated scopes.
                *
                *
                *
                * */
				break;
			case REGISTER:
				redirectURIString = AppConfigSingleton.INSTANCE.getSsoRedirectURI();
				break;


			case UPDATE_PASSWORD:
				redirectURIString = AppConfigSingleton.INSTANCE.getSsoUpdateDetailsRedirectUri();
				break;
			case UPDATE_PROFILE:
				redirectURIString = AppConfigSingleton.INSTANCE.getSsoUpdateDetailsRedirectUri();
				break;

			case LOGOUT:
				redirectURIString = AppConfigSingleton.INSTANCE.getSsoRedirectURILogout();
				break;

			case FORGOT_PASSWORD:
				return forgotPassword;

			default:
				break;
		}

		if (scope == null) {
			scope = "";
		}

		scope = ("openid email profile") + " " + scope;//default scope
		scope = scope.trim();

		Uri.Builder builder = new Uri.Builder();
		if (this.host != null) {
			builder.scheme(this.host.rawValue()) // moved host.rawValue() from authority to schema as MCS returns host with " https:// "
					.appendEncodedPath(this.path.rawValue())
					.appendQueryParameter("client_id", "WWOneApp")
					.appendQueryParameter("response_type", "id_token") // Identity token
					.appendQueryParameter("response_mode", "form_post")
					.appendQueryParameter("redirect_uri", redirectURIString)
					.appendQueryParameter("state", this.state)
					.appendQueryParameter("nonce", this.nonce)
					.appendQueryParameter("scope", scope)
					.appendQueryParameter("appVersion", AppConfigSingleton.INSTANCE.getStsValues().getKmsiMinimumSupportedAppVersion() != null ? AppConfigSingleton.INSTANCE.getStsValues().getKmsiMinimumSupportedAppVersion() : WoolworthsApplication.getAppVersionName());
		}

		if (this.extraQueryStringParams != null) {
			for (Map.Entry<String, String> param : this.extraQueryStringParams.entrySet()) {
				builder.appendQueryParameter(param.getKey(), param.getValue());
			}
		}

		String constructedURL = "";
		try {
			constructedURL = URLDecoder.decode(builder.build().toString(), "UTF-8").toString();
		} catch (Exception e) {
			constructedURL = builder.build().toString();
		}
		return constructedURL;
	}

	/* Check whether the URL that we pass in
	 * is one of the URLs that we're listing for.
	 * The URL parameter may contain url-encoded characters.
	 *
	 * When this method is called with a null state
	 * the local redirectURL is null, do nothing and log to firebase.
	 * */
	private boolean isNavigatingToRedirectURL(String url) {
		//Fixes WOP-3286
		if (redirectURIString == null || this.state == null){
			//report this to analytics
			Map<String, String> arguments = new HashMap<>();
			arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.DESCRIPTION,
					"redirectURIString isNull: " + (redirectURIString == null ? "true" : "false") + ";" +
							"this.state isNull: " + (this.state == null ? "true" : "false"));

			Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.CRASH_CAUTION, arguments, SSOActivity.this);
			return false;
		}

		String redirectUriWithState = redirectURIString.concat("?state=").concat(this.state);

		return url.equalsIgnoreCase(redirectURIString) || url.equalsIgnoreCase(redirectUriWithState);
	}

	private final WebViewClient webviewClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (!url.contains("logout"))
				showProgressBar();
			stsParams = SessionUtilities.getInstance().getSTSParameters();

			if (extraQueryStringParams != null) {
				int indexOfQuestionMark = url.indexOf("?");
				if (indexOfQuestionMark > -1) {
					String urlWithoutQueryString = url.substring(0, indexOfQuestionMark);

					if (urlWithoutQueryString.equals(extraQueryStringParams.get("post_logout_redirect_uri"))) {
						SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
						ServiceTools.Companion.stop(SSOActivity.this, LiveChatService.class);
						OCConstant.Companion.stopOCChatService(SSOActivity.this);
						Utils.removeFromDb(SessionDao.KEY.SHOP_OPTIMISER_SQLITE_MODEL);
						Intent intent = new Intent();
						setResult(SSOActivityResult.SIGNED_OUT.rawValue(), intent);
						Utils.setUserKMSIState(false);
						AppConfigSingleton.INSTANCE.setBadgesRequired(true);
						clearAllCookies();
						closeActivity();
					} else {
					}
				}

			} else if (url.equalsIgnoreCase(AppConfigSingleton.INSTANCE.getSsoUpdateDetailsRedirectUri())) {
				setResult(SSOActivityResult.CHANGE_PASSWORD.rawValue());
				closeActivity();
			}
		}

		@Nullable
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

			//fixes WOP-4401
			extractFormDataOnUIThreadForLoginRegisterAndCloseSSOIfNeeded();
			return super.shouldInterceptRequest(view, url);
		}

		@Nullable
		@Override
		public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {

			extractFormDataOnUIThreadForLoginRegisterAndCloseSSOIfNeeded();
			return super.shouldInterceptRequest(view, request);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			if(url.contains("/customerid/login") || url.contains("/customerid/userdetails") || url.contains("/customerid/userdetails/password")){
				handleUIForKMSIEntry(false);
			}

			if (SSOActivity.this.path == Path.SIGNIN) {
				view.evaluateJavascript("jquery:$('#rememberMe').on('change', function() {injection.reportCheckboxStateChange($(this).is(':checked'))})", null);
				view.evaluateJavascript("jquery:injection.reportCheckboxState($('#rememberMe').is(':checked'))", null);
			}

			if (isNavigatingToRedirectURL(url)) {
				//get state and scope from webview posted form
				if (SSOActivity.this.path.rawValue().equals(Path.LOGOUT.rawValue())) {
					KotlinUtils.setUserPropertiesToNull();
					ServiceTools.Companion.stop(SSOActivity.this, LiveChatService.class);
					OCConstant.Companion.stopOCChatService(SSOActivity.this);
					Intent intent = new Intent();
					setResult(SSOActivityResult.SIGNED_OUT.rawValue(), intent);

				} else if (SSOActivity.this.path.rawValue().equals(Path.UPDATE_PROFILE.rawValue()) || SSOActivity.this.path.rawValue().equals(Path.UPDATE_PASSWORD.rawValue())) {
							/*Intent intent = new Intent();
							setResult(SSOActivityResult.CHANGE_PASSWORD.rawValue(), intent);*/
				}
			}
			hideProgressBar();
			if (Boolean.TRUE.equals(AppConfigSingleton.getDynamicYieldConfig().isDynamicYieldEnabled())) {
				prepareDynamicYieldRequestEvent();
			}
		}

		@TargetApi(android.os.Build.VERSION_CODES.M)
		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			super.onReceivedError(view, request, error);
			showFailureView(error.toString());
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
			showFailureView(description);
		}


		@Override
		public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
			//super.onReceivedSslError(view, handler, error);
			hideProgressBar();
			final AlertDialog.Builder builder = new AlertDialog.Builder(SSOActivity.this);
			builder.setMessage(R.string.ssl_error);
			builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handler.proceed();
				}
			});
			builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handler.cancel();
				}
			});
			final AlertDialog dialog = builder.create();
			dialog.show();
		}

		@Override @TargetApi(21)
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			final String url = request.getUrl().toString();

			if (canOpenDialerForString(url)){
				openDialerWithString(url);
				return true;
			}

			return super.shouldOverrideUrlLoading(view, request);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//shouldOverrideUrlLoading(WebView view, String url) is deprecated
			//in api 24. shouldOverrideUrlLoading(WebView view, WebResourceRequest request) would
			//have been called already.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && canOpenDialerForString(url)){
				openDialerWithString(url);
				return true;
			}

			return super.shouldOverrideUrlLoading(view, url);
		}

		private boolean canOpenDialerForString(final String string){
			return string.startsWith("tel:");
		}

		private void openDialerWithString(final String string){
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse(string));
			startActivity(intent);
		}
	};

	private void extractFormDataOnUIThreadForLoginRegisterAndCloseSSOIfNeeded(){
		if (SSOActivity.this.path == Path.SIGNIN || SSOActivity.this.path == Path.REGISTER) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					extractFormDataAndCloseSSOIfNeeded(String.valueOf(SSOActivity.this.path));
				}
			});

		}
	}

	private void extractFormDataAndCloseSSOIfNeeded(String ssoActivityEvent){
		if (Utils.getDyServerId() != null) {
			dyServerId = Utils.getDyServerId();
		} else {
			dyServerId = "";
		}
		if (Utils.getDySessionId() != null) {
			dySessionId = Utils.getDySessionId();
		} else {
			dySessionId = "";
		}
		SSOActivity.this.webView.evaluateJavascript("(function(){return {'content': [document.forms[0].state.value.toString(), document.forms[0].id_token.value.toString()]}})();", new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {

				//this is sign in and
				//isExtractFormDataCompleted - ensure one time value received
				if (value.equals("null") || isExtractFormDataCompleted)
					return;

				isExtractFormDataCompleted = true;

				JsonParser jsonParser = new JsonParser();
				JsonObject jsonObject = (JsonObject) jsonParser.parse(value);
				ArrayList<String> list = new Gson().fromJson(jsonObject.getAsJsonArray("content"), ArrayList.class);

				String webviewState = list.get(0);

				Intent intent = new Intent();
				JWTDecodedModel jwtDecodedModel = null;

				if (state.equals(webviewState)) {
					jwt = list.get(1);
					intent.putExtra(SSOActivity.TAG_JWT, jwt);
					//Save JWT
					SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.USER_TOKEN);
					sessionDao.value = jwt;
					try {
						sessionDao.save();
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}

					//Trigger Firebase Tag.
					 jwtDecodedModel = SessionUtilities.getInstance().getJwt();
					/*if (jwtDecodedModel != null) {
						String email = jwtDecodedModel.email.get(0);
						Log.d(TAG, "onReceiveValue: mailid" +email);
					}*/
					Map<String, String> arguments = new HashMap<>();
					arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, (jwtDecodedModel.C2Id != null) ? jwtDecodedModel.C2Id : "");
					Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.LOGIN, arguments, SSOActivity.this);

					notificationUtils.sendRegistrationToServer();

					SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.ACTIVE);
					QueryBadgeCounter.getInstance().queryBadgeCount();
					setUserATGId(jwtDecodedModel);
					Utils.setUserKMSIState(isKMSIChecked);
					if (KotlinUtils.Companion.getAnonymousUserLocationDetails() != null) {
						new ConfirmLocation().postRequest(KotlinUtils.Companion.getAnonymousUserLocationDetails(), isUserBrowsing, SSOActivity.this, intent);
						try {
							if (!TextUtils.isEmpty(stsParams)) {
								SessionUtilities.getInstance().setSTSParameters(null);
							}
						} catch (NullPointerException ex) {
							closeActivity();
						}
					} else {
						setSignInSuccessful(intent);
						startOCDashChatServices();
						setStSParameters();
					}

				} else {
					setResult(SSOActivityResult.STATE_MISMATCH.rawValue(), intent);
					setStSParameters();
				}
				if (ssoActivityEvent == "SIGNIN") {
					if (Boolean.TRUE.equals(AppConfigSingleton.getDynamicYieldConfig().isDynamicYieldEnabled())) {
						String hexvalue = null;
						if (jwtDecodedModel != null) {
							hexvalue = sha256Value(jwtDecodedModel.email.get(0));
						}
						prepareDySigninRequestEvent(hexvalue);
						prepareDyIdentifyUserRequestEvent(hexvalue);
					}

				}else if (ssoActivityEvent == "REGISTER") {
					if (Boolean.TRUE.equals(AppConfigSingleton.getDynamicYieldConfig().isDynamicYieldEnabled())) {
						String hexvalue = null;
						if (jwtDecodedModel != null) {
							hexvalue = sha256Value(jwtDecodedModel.email.get(0));
						}
						prepareDyRegisterRequestEvent(hexvalue);
						prepareDyIdentifyUserRequestEvent(hexvalue);
					}
				}
			}
		});
	}

	private void setSignInSuccessful(Intent intent) {
		AuthenticateUtils.Companion.enableBiometricForCurrentSession(true);
		setResult(SSOActivityResult.SUCCESS.rawValue(), intent);
	}

	private String sha256Value(String s) {
		try{
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(s.getBytes("UTF-8"));
			final StringBuilder hexString = new StringBuilder();
			for (int i = 0; i < hash.length; i++) {
				final String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	private void prepareDySigninRequestEvent(String hashMail)  {
		User user = new User(dyServerId,dyServerId);
		Session session = new Session(dySessionId);
		Device device = new Device(IPAddress,config.getDeviceModel());
		Context context = new Context(device,null, DY_CHANNEL,null);
		Properties properties = new Properties(null,null,LOGIN_V1,null,null,null,null,null,null,null,hashMail,null,null,null,null,null,null,null);
		Event event = new Event(null,null,null,null,null,null,null,null,null,null,null,null,LOGIN,properties);
		ArrayList<Event> eventArrayList = new ArrayList<>();
		eventArrayList.add(event);
		PrepareChangeAttributeRequestEvent prepareLoginDYRequestEvent = new PrepareChangeAttributeRequestEvent(
				context,
				eventArrayList,
				session,
				user
		);
		dyReportEventViewModel.createDyChangeAttributeRequest(prepareLoginDYRequestEvent);

	}

	private void prepareDyIdentifyUserRequestEvent(String hashMail) {
		User user = new User(dyServerId,dyServerId);
		Session session = new Session(dySessionId);
		Device device = new Device(IPAddress, config.getDeviceModel());
		Context context = new Context(device,null, DY_CHANNEL,null);
		Properties properties = new Properties(null,null,IDENTIFY_V1,null,null,null,null,null,null,null,hashMail,null,null,null,null,null,null,null);
		Event event = new Event(null,null,null,null,null,null,null,null,null,null,null,null,IDENTIFY,properties);
		ArrayList<Event> eventArrayList = new ArrayList<>();
		eventArrayList.add(event);
		PrepareChangeAttributeRequestEvent prepareLoginDYRequestEvent = new PrepareChangeAttributeRequestEvent(
				context,
				eventArrayList,
				session,
				user
		);
		dyReportEventViewModel.createDyChangeAttributeRequest(prepareLoginDYRequestEvent);
	}

	private void prepareDyRegisterRequestEvent(String hashMail) {
		User user = new User(dyServerId,dyServerId);
		Session session = new Session(dySessionId);
		Device device = new Device(IPAddress, config.getDeviceModel());
		Context context = new Context(device,null, DY_CHANNEL,null);
		Properties properties = new Properties(null,null,SIGNUP_V1,null,null,null,null,null,null,null,hashMail,null,null,null,null,null,null,null);
		Event event = new Event(null,null,null,null,null,null,null,null,null,null,null,null,SIGNUP,properties);
		ArrayList<Event> eventArrayList = new ArrayList<>();
		eventArrayList.add(event);
		PrepareChangeAttributeRequestEvent prepareLoginDYRequestEvent = new PrepareChangeAttributeRequestEvent(
				context,
				eventArrayList,
				session,
				user
		);
		dyReportEventViewModel.createDyChangeAttributeRequest(prepareLoginDYRequestEvent);
	}

	private void setStSParameters() {
		try {
			if (!TextUtils.isEmpty(stsParams)) {
				SessionUtilities.getInstance().setSTSParameters(null);
			}

			closeActivity();

		} catch (NullPointerException ex) {
			closeActivity();
		}
	}

	private void setUserATGId(JWTDecodedModel jwtDecodedModel) {
		String atgId = (jwtDecodedModel.AtgId.isJsonArray() ? jwtDecodedModel.AtgId.getAsJsonArray().get(0).getAsString() : jwtDecodedModel.AtgId.getAsString());
		AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.ATGId, atgId);
		AnalyticsManager.Companion.setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, jwtDecodedModel.C2Id);
		AnalyticsManager.Companion.setUserId(atgId);
		FirebaseManager.Companion.setCrashLyticsUserId(atgId);
	}

	private void unknownNetworkFailure(WebView webView, String description) {
		if (!NetworkManager.getInstance().isConnectedToNetwork(SSOActivity.this)) {
			mErrorHandlerView.webViewBlankPage(webView);
			mErrorHandlerView.networkFailureHandler(description);
		}
	}

	private void unKnownNetworkFailure(WebView view, WebResourceError error) {
		try {
			unknownNetworkFailure(view, error.toString());
		} catch (NullPointerException ex) {
		}
	}

	public void hideProgressBar() {
		toggleLoading(false);
	}

	public void showProgressBar() {
		toggleLoading(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					if (this.webView != null && webView.canGoBack()) {
						webView.goBack();
					} else {
						finishActivity();
					}
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (this.webView != null && webView.canGoBack() && toolbarTextView.getText().toString().contains(TNC_TITLE)) {
					webView.goBack();
				} else {
					finishActivity();
				}
				break;
		}
		return true;

	}

	@Override
	protected void onResume() {
		super.onResume();
		if(path == Path.SIGNIN) {
			Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.SSO_SIGN_IN);
		} else if(path == Path.REGISTER) {
			Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.SSO_REGISTER);
		} else if(path == Path.LOGOUT) {
			Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.SSO_LOGOUT);
		} else if(path == Path.UPDATE_PASSWORD) {
			Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.SSO_PASSWORD_CHANGE);
		} else if(path == Path.UPDATE_PROFILE) {
			Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.SSO_PROFILE_INFO);
		}
		else if(path == Path.FORGOT_PASSWORD) {
			Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.SSO_FORGOT_PASSWORD);
		}
	}

	public void closeActivity() {
		finish();
		overridePendingTransition(0, 0);
	}

	@Override
	protected void onDestroy() {
		if (this.webView != null) {
			// handle  WebView.destroy() called while WebView is still attached to window.
			this.webView.removeAllViews();
			this.webView.destroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()){
			setSignInSuccessful(data);
			setStSParameters();
		}
	}

	private void clearAllCookies() {
		CookieManager.getInstance().removeAllCookies(null);
		CookieManager.getInstance().flush();
	}

	private void showFailureView(String s) {
		if (!NetworkManager.getInstance().isConnectedToNetwork(SSOActivity.this)) {
			//This handle UI to show the Action bar when UI is transparent
			handleUIForKMSIEntry(false);
			mErrorHandlerView.networkFailureHandler(s);
		}
	}

	public void finishActivity() {
		SessionUtilities.getInstance().setSTSParameters(null);
		setResult(DEFAULT_KEYS_SEARCH_GLOBAL);
		finish();
		if (this.path != null && (this.path == SSOActivity.Path.UPDATE_PASSWORD || this.path == SSOActivity.Path.UPDATE_PROFILE))
			overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
		else
			overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
	}

	class KMSIState extends Object{
		@JavascriptInterface
		public void reportCheckboxState(boolean isChecked) {
			isKMSIChecked = isChecked;
		}

		@JavascriptInterface
		public void reportCheckboxStateChange(boolean isChecked) {
			isKMSIChecked = isChecked;
		}
	}

	public void handleUIForKMSIEntry(boolean showKMSIView) {
		ssoLayout.setVisibility(showKMSIView ? View.GONE : View.VISIBLE);
		loadingProgressBarKMSI.setVisibility(showKMSIView ? View.VISIBLE : View.GONE);
	}


	@Override
	public void onAttachedToWindow() {
		getTheme().applyStyle(isKMSIChecked ? R.style.SSOActivityKMSIStyle : R.style.SSOActivity, true);
		super.onAttachedToWindow();
	}

	private void startOCDashChatServices() {
		// Start service to listen to incoming messages from Stream
		OCConstant.Companion.startOCChatService(this);
	}

	private void prepareDynamicYieldRequestEvent() {
		ArrayList<String> dyData = new ArrayList<>();
		Device device = new Device(Utils.IPAddress, config.getDeviceModel());
		Page page = new Page(dyData, Utils.MOBILE_LANDING_PAGE, Utils.HOME_PAGE, null, null);
		Context context = new Context(device, page, Utils.DY_CHANNEL, null);
		Options options = new Options(true);
		HomePageRequestEvent homePageRequestEvent = new HomePageRequestEvent(null, null, context, options);
		dyHomePageViewModel.createDyRequest(homePageRequestEvent);
	}

}
