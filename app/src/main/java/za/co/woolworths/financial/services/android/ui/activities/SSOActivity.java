package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import io.jsonwebtoken.Jwts;
import za.co.woolworths.financial.services.android.util.JWTHelper;
import za.co.woolworths.financial.services.android.util.SSORequiredParameter;

public class SSOActivity extends WebViewActivity {

    public static enum SSOActivityResult {
        LAUNCH(1),
        NO_CACHED_STATE(2),
        NO_CACHED_NONCE(3),
        STATE_MISMATCH(4),
        NONCE_MISMATCH(5),
        SUCCESS(6);

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


    // TODO: This redirectURIString be pulled from MCS.
    private String redirectURIString = "http://wfs-appserver-dev.wigroup.co:8080/wfs/app/v4/sso/redirect/successful";
    private Protocol protocol;
    private Host host;
    private Path path;

    private final String state = UUID.randomUUID().toString();
    private final String nonce = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        String link = this.constructAndGetAuthorisationRequestURL();

        Log.d(SSOActivity.TAG, String.format("Authorization Link: %s", link));

        bundle.putString("title", "SSO");
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
        STS("sts.woolworths.co.za");

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
        SIGNIN("/customerid/connect/authorize"),
        REGISTER("/customerid/register/step1");

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

    private String constructAndGetAuthorisationRequestURL() {
        String scope = "openid email profile";//default scope


        Uri.Builder builder = new Uri.Builder();
        builder.scheme(this.protocol.rawValue())
                .authority(this.host.rawValue())
                .appendPath(this.path.rawValue())
                .appendQueryParameter("client_id", "WWOneApp")
                .appendQueryParameter("response_type", "id_token") // Identity token
                .appendQueryParameter("response_mode", "form_post")
                .appendQueryParameter("redirect_uri", this.redirectURIString)
                .appendQueryParameter("state", this.state)
                .appendQueryParameter("nonce", this.nonce)
                .appendQueryParameter("scope", scope)
        ;

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


        return builder.build().toString();
    }

    private final WebViewClient webviewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, final String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            Log.d(TAG, url);

            if (url.equals(SSOActivity.this.redirectURIString)) {
                //get state and scope from webview posted form
                view.evaluateJavascript("(function(){return {'content': [document.forms[0].state.value.toString(), document.forms[0].id_token.value.toString()]}})();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(value);
                        ArrayList<String> list = new Gson().fromJson(jsonObject.getAsJsonArray("content"), ArrayList.class);

                        String webviewState = list.get(0);

                        Intent intent = new Intent();

                        if (SSOActivity.this.state.equals(webviewState)) {

                            String jwt = list.get(1);
                            intent.putExtra(SSOActivity.TAG_JWT, jwt);

                            setResult(SSOActivityResult.SUCCESS.rawValue(), intent);
                        } else {
                            setResult(SSOActivityResult.STATE_MISMATCH.rawValue(), intent);
                        }
                        finish();
                    }
                });
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //super.onReceivedSslError(view, handler, error);
            Log.e(TAG, "[onReceivedSslError]: " + error.toString());
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };
}
