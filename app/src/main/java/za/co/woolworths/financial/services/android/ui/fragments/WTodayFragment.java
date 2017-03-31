package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

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
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(WoolworthsApplication.getWwTodayURI());
        webView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
        return view;
    }
}
