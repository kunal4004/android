package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

/**
 * Created by W7099877 on 02/12/2016.
 */

public class WTodayFragment extends Fragment {

    WebView webView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.wtoday_fragment, container, false);

        //getActivity().getWindow().requestFeature(Window.FEATURE_PROGRESS);
        webView = (WebView)view.findViewById(R.id.wtoday_fragment_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(WoolworthsApplication.getWwTodayURI());

        return view;
    }
}
