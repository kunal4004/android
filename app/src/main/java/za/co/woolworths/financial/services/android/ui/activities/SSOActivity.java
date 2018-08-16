package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter;
import za.co.woolworths.financial.services.android.util.SSORequiredParameter;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

public class SSOActivity extends WebViewActivity {

	public ErrorHandlerView mErrorHandlerView;
	private WGlobalState mGlobalState;

	public enum SSOActivityResult {
		LAUNCH(1),
		NO_CACHED_STATE(2),
		NO_CACHED_NONCE(3),
		STATE_MISMATCH(4),
		NONCE_MISMATCH(5),
		SUCCESS(6),
		SIGNED_OUT(8),
		CHANGE_PASSWORD(9);
		private int result;

		private SSOActivityResult(int i) {
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
	public static final String TAG_EXTRA_QUERYSTRING_PARAMS = "TAG_EXTRA_QUERYSTRING_PARAMS";
	//Default redirect url used by LOGIN AND LINK CARDS
	private static String redirectURIString = WoolworthsApplication.getSsoRedirectURI();
	private Protocol protocol;
	private Host host;
	public Path path;
	private Map<String, String> extraQueryStringParams;

	private final String state;
	private final String nonce;
	private String stsParams;

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
	}

	private void instantiateWebView() {
		this.webView.setWebViewClient(this.webviewClient);
		this.webView.getSettings().setUseWideViewPort(true);
		this.webView.getSettings().setLoadWithOverviewMode(true);
		this.webView.getSettings().setDomStorageEnabled(true);
		if (Build.VERSION.SDK_INT >= 21) {
			this.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
		this.webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);

				ArrayList<String> invalidTitles = new ArrayList<String>(
						Arrays.asList("about:blank".toLowerCase(),
								getString(R.string.sso_title_text_submit_this_form).toLowerCase(),
								SSOActivity.this.redirectURIString.toLowerCase(),
								SSOActivity.this.redirectURIString.concat("?state=").concat(SSOActivity.this.state).toLowerCase())
				);

				if (invalidTitles.contains(title.toLowerCase())) {
					toolbarTextView.setText("");
				} else
					toolbarTextView.setText(title);
			}
		});
		retryConnect();
	}

	private void retryConnect() {
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(SSOActivity.this)) {
					WebBackForwardList history = webView.copyBackForwardList();
					int index = -1;
					String url;
					while (webView.canGoBackOrForward(index)) {
						if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
							webView.goBackOrForward(index);
							url = history.getItemAtIndex(-index).getUrl();
							Log.d("tag", "first non empty" + url);
							break;
						}
						index--;
					}
					mErrorHandlerView.hideErrorHandlerLayout();
				}
			}
		});
	}

	//override intent to return expected link that's to be used in the WebViewActivity
	@Override
	public Intent getIntent() {
		Intent intent = super.getIntent();
		Bundle bundle = intent.getExtras();

		this.protocol = Protocol.getProtocolByRawValue(bundle.getString(SSOActivity.TAG_PROTOCOL));
		this.host = Host.getHostByRawValue(bundle.getString(SSOActivity.TAG_HOST));
		this.path = Path.getPathByRawValue(bundle.getString(SSOActivity.TAG_PATH));
		this.extraQueryStringParams = (Map<String, String>) intent.getSerializableExtra(SSOActivity.TAG_EXTRA_QUERYSTRING_PARAMS);

		String scope = bundle.getString(SSOActivity.TAG_SCOPE);
		String link = this.constructAndGetAuthorisationRequestURL(scope);

		Log.d("SSOActivity.TAG", String.format("Authorization Link: %s", link));

		Log.d(SSOActivity.TAG, String.format("Authorization Link: %s", link));

		bundle.putString("title", "SIGN IN");
		bundle.putString("link", link);
		intent.putExtra("Bundle", bundle);

		return intent;
	}

	public enum Protocol implements SSORequiredParameter {
		HTTP("http"),
		HTTPS("https");

		private String protocol;

		private Protocol(String protocol) {
			this.protocol = protocol;
		}

		public String rawValue() {
			return protocol;
		}

		public static Protocol getProtocolByRawValue(String rawValue) {
			for (Protocol p : Protocol.values()) {
				if (rawValue.equals(p.rawValue()))
					return p;
			}
			return null;
		}
	}

	public enum Host implements SSORequiredParameter {
		STS(WoolworthsApplication.getStsURI());

		private String host;

		private Host(String protocol) {
			this.host = protocol;
		}

		@Override
		public String rawValue() {
			return host;
		}

		public static Host getHostByRawValue(String rawValue) {
			if (rawValue == null) return null;
			if (Host.values() == null) return null;
			for (Host h : Host.values()) {
				if (rawValue.equals(h.rawValue()))
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
		UPDATE_PROFILE("customerid/userdetails");

		private String path;

		private Path(String protocol) {
			this.path = protocol;
		}

		public String rawValue() {
			return this.path;
		}

		public static Path getPathByRawValue(String rawValue) {
			for (Path p : Path.values()) {
				if (rawValue.equals(p.rawValue()))
					return p;
			}
			return null;
		}
	}

	private String constructAndGetAuthorisationRequestURL(String scope) {
		if (this.path == null) return "";
		switch (this.path) {

			case SIGNIN:
				this.redirectURIString = WoolworthsApplication.getSsoRedirectURI();

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
				this.redirectURIString = WoolworthsApplication.getSsoRedirectURI();
				break;


			case UPDATE_PASSWORD:
				this.redirectURIString = WoolworthsApplication.getSsoUpdateDetailsRedirectUri();
				break;
			case UPDATE_PROFILE:
				this.redirectURIString = WoolworthsApplication.getSsoUpdateDetailsRedirectUri();
				break;

			case LOGOUT:
				this.redirectURIString = WoolworthsApplication.getSsoRedirectURILogout();
				break;

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
					.appendQueryParameter("redirect_uri", this.redirectURIString)
					.appendQueryParameter("state", this.state)
					.appendQueryParameter("nonce", this.nonce)
					.appendQueryParameter("scope", scope);
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

	private final WebViewClient webviewClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (!url.contains("logout"))
				showProgressBar();
			stsParams = SessionUtilities.getInstance().getSTSParameters();

			if (SSOActivity.this.path == Path.SIGNIN || SSOActivity.this.path == Path.REGISTER) {
				view.evaluateJavascript("(function(){return {'content': [document.forms[0].state.value.toString(), document.forms[0].id_token.value.toString()]}})();", new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						//this is sign in
						if (value.equals("null"))
							return;

						JsonParser jsonParser = new JsonParser();
						JsonObject jsonObject = (JsonObject) jsonParser.parse(value);
						ArrayList<String> list = new Gson().fromJson(jsonObject.getAsJsonArray("content"), ArrayList.class);

						String webviewState = list.get(0);

						Intent intent = new Intent();

						if (state.equals(webviewState)) {
							String jwt = list.get(1);
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
							JWTDecodedModel jwtDecodedModel = SessionUtilities.getInstance().getJwt();
							Map<String, String> arguments = new HashMap<>();
							arguments.put("c2_id", (jwtDecodedModel.C2Id != null) ? jwtDecodedModel.C2Id : "");
							Utils.triggerFireBaseEvents(getApplicationContext(), FirebaseAnalytics.Event.LOGIN, arguments);

							NotificationUtils.getInstance().sendRegistrationToServer();
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.ACTIVE);
							QueryBadgeCounter.getInstance().queryBadgeCount();
							setResult(SSOActivityResult.SUCCESS.rawValue(), intent);
						} else {
							setResult(SSOActivityResult.STATE_MISMATCH.rawValue(), intent);
						}

						try {
							if (!TextUtils.isEmpty(stsParams)) {
								SessionUtilities.getInstance().setSTSParameters(null);
							}
							closeActivity();

						} catch (NullPointerException ex) {
							closeActivity();
						}
					}
				});
			} else if (extraQueryStringParams != null) {
				int indexOfQuestionMark = url.indexOf("?");
				if (indexOfQuestionMark > -1) {
					String urlWithoutQueryString = url.substring(0, indexOfQuestionMark);

					if (urlWithoutQueryString.equals(extraQueryStringParams.get("post_logout_redirect_uri"))) {
						SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
						Intent intent = new Intent();
						setResult(SSOActivityResult.SIGNED_OUT.rawValue(), intent);
						closeActivity();
					} else {
					}
				}

			} else if (url.equalsIgnoreCase(WoolworthsApplication.getSsoUpdateDetailsRedirectUri())) {
				setResult(SSOActivityResult.CHANGE_PASSWORD.rawValue());
				closeActivity();
			} else {
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (isNavigatingToRedirectURL(url)) {
				//get state and scope from webview posted form
				if (SSOActivity.this.path.rawValue().equals(Path.LOGOUT.rawValue())) {
					Intent intent = new Intent();
					setResult(SSOActivityResult.SIGNED_OUT.rawValue(), intent);

				} else if (SSOActivity.this.path.rawValue().equals(Path.UPDATE_PROFILE.rawValue()) || SSOActivity.this.path.rawValue().equals(Path.UPDATE_PASSWORD.rawValue())) {
							/*Intent intent = new Intent();
							setResult(SSOActivityResult.CHANGE_PASSWORD.rawValue(), intent);*/
				} else {
				}
			}
			hideProgressBar();
		}

		private boolean isNavigatingToRedirectURL(String url) {

			String redirectUriWithState = SSOActivity.this.redirectURIString.concat("?state=").concat(SSOActivity.this.state);

			return url.equalsIgnoreCase(SSOActivity.this.redirectURIString) || url.equalsIgnoreCase(redirectUriWithState);
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

	};



	private void unknownNetworkFailure(WebView webView, String description) {
		if (!new ConnectionDetector().isOnline(SSOActivity.this)) {
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
					finishActivity();
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finishActivity();
				break;
		}
		return true;

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

	private void clearHistory() {
		mGlobalState.setOnBackPressed(false);
		Intent i = new Intent(SSOActivity.this, BottomNavigationActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		closeActivity();
	}

	private void showFailureView(String s) {
		if (!new ConnectionDetector().isOnline(SSOActivity.this))
			mErrorHandlerView.networkFailureHandler(s);
	}
}
