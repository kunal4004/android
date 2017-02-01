package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import io.jsonwebtoken.Jwts;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SSORequiredParameter;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.R.attr.data;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class SSOActivity extends WebViewActivity {

    public static enum SSOActivityResult {
        LAUNCH(1),
        NO_CACHED_STATE(2),
        NO_CACHED_NONCE(3),
        STATE_MISMATCH(4),
        NONCE_MISMATCH(5),
        SUCCESS(6),
        EXPIRED(7),
        SIGNED_OUT(8);

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

    private Toolbar mToolbar;
    // TODO: This redirectURIString be pulled from MCS.
    private String redirectURIString = "http://wfs-appserver-dev.wigroup.co:8080/wfs/app/v4/sso/redirect/successful";
    private Protocol protocol;
    private Host host;
    private Path path;
    private Map<String, String> extraQueryStringParams;

    private final String state;
    private final String nonce;
    public ProgressDialog progressDialog;

    public SSOActivity() {
        this.state = UUID.randomUUID().toString();
        this.nonce = UUID.randomUUID().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instantiateWebView();
    }

    private void instantiateWebView() {
        progressDialog = new ProgressDialog(SSOActivity.this, R.style.full_screen_dialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.sso_progress_dialog);
                getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
                mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            }
        };
        progressDialog.setCancelable(false);
        this.webView.setWebViewClient(this.webviewClient);
        this.webView.getSettings().setUseWideViewPort(true);
        this.webView.getSettings().setLoadWithOverviewMode(true);
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
        STS("stsqa.woolworths.co.za");

        private String host;

        private Host(String protocol) {
            this.host = protocol;
        }

        @Override
        public String rawValue() {
            return host;
        }

        public static Host getHostByRawValue(String rawValue) {
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
        LOGOUT("customerid/connect/endsession");

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

        if (scope == null) {
            scope = "";
        }
        scope = scope.concat(" openid email profile");//default scope


        Uri.Builder builder = new Uri.Builder();
        builder.scheme(this.protocol.rawValue())
                .authority(this.host.rawValue())
                .appendEncodedPath(this.path.rawValue())
                .appendQueryParameter("client_id", "WWOneApp")
                .appendQueryParameter("response_type", "id_token") // Identity token
                .appendQueryParameter("response_mode", "form_post")
                .appendQueryParameter("redirect_uri", this.redirectURIString)
                .appendQueryParameter("state", this.state)
                .appendQueryParameter("nonce", this.nonce)
                .appendQueryParameter("scope", scope)
        ;

        if (this.extraQueryStringParams != null) {
            for (Map.Entry<String, String> param : this.extraQueryStringParams.entrySet()) {
                builder.appendQueryParameter(param.getKey(), param.getValue());
            }
        }

        switch (this.path) {

            case SIGNIN:

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
                break;

            default:
                break;
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
        public void onPageStarted(WebView view, final String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgressBar();
            Log.d(TAG, url);

            if (url.equals(SSOActivity.this.redirectURIString)) {
                //get state and scope from webview posted form
                view.evaluateJavascript("(function(){return {'content': [document.forms[0].state.value.toString(), document.forms[0].id_token.value.toString()]}})();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (value == "null")
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
                            SessionDao sessionDao = new SessionDao(SSOActivity.this, SessionDao.KEY.USER_TOKEN);
                            sessionDao.value = jwt;
                            try {
                                sessionDao.save();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            sendRegistrationToServer();
                            setResult(SSOActivityResult.SUCCESS.rawValue(), intent);
                        } else {
                            setResult(SSOActivityResult.STATE_MISMATCH.rawValue(), intent);
                        }
                        finish();
                    }
                });
            } else if (extraQueryStringParams != null) {
                int indexOfQuestionMark = url.indexOf("?");
                if (indexOfQuestionMark > -1) {
                    String urlWithoutQueryString = url.substring(0, indexOfQuestionMark);

                    String redirectURI = extraQueryStringParams.get("post_logout_redirect_uri");
                    if (urlWithoutQueryString.equals(redirectURI)) {
                        Intent intent = new Intent();
                        setResult(SSOActivityResult.SIGNED_OUT.rawValue(), intent);
                        finish();
                    }
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            hideProgressBar();
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //super.onReceivedSslError(view, handler, error);
            hideProgressBar();
            handler.proceed();
        }
    };

    public void hideProgressBar() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    public void showProgressBar() {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }
    }


    //1. sendRegistrationToServer is created twice: SSOActivity and WFirebaseInstanceIDSService
    //


    private void sendRegistrationToServer() {
        // sending gcm token to server
        final CreateUpdateDevice device = new CreateUpdateDevice();
        device.appInstanceId = InstanceID.getInstance(getApplicationContext()).getId();
        device.pushNotificationToken = getSharedPreferences(Utils.SHARED_PREF, 0).getString("regId", null);

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
