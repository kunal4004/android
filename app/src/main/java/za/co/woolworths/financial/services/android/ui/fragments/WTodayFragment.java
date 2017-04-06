package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.WebAppInterface;

public class WTodayFragment extends Fragment {

    WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wtoday_fragment, container, false);
        webView = (WebView) view.findViewById(R.id.wtoday_fragment_webview);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
        webView.loadUrl(WoolworthsApplication.getWwTodayURI());
        return view;
    }


    public class MyWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.e("URLWebviews", url);
            // startActivity(new Intent(Intent.ACTION_VIEW,
            // Uri.parse("https://www.youtube.com/embed/tS4biyoiMN0?enablejsapi=1&origin=https%3A%2F%2Fwfs-tww.wigroup.co&widgetid=3")));
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("URLWebview", url);
            return true;
        }
    }
}
