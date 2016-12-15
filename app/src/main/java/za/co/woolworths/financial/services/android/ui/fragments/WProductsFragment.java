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
import android.webkit.WebViewClient;

import com.awfs.coordination.R;

/**
 * Created by W7099877 on 02/12/2016.
 */

public class WProductsFragment extends Fragment {

    WebView webView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wproducts_fragment, container, false);

        //getActivity().getWindow().requestFeature(Window.FEATURE_PROGRESS);
        this.webView = (WebView)view.findViewById(R.id.wproducts_fragment_webview);
        Log.d("", getString(R.string.link_products));
        this.webView.loadUrl(getString(R.string.link_products));
             webView.getSettings().setJavaScriptEnabled(true);


        return view;
    }
}
