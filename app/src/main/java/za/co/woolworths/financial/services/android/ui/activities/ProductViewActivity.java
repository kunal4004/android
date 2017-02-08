package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.AdvancedWebView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductViewActivity extends BaseActivity {
    private Toolbar toolbar;
    private WTextView mToolBarTitle;
    private AdvancedWebView productListWebView;
    private String productId;
    private String productName;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Utils.updateStatusBarBackground(ProductViewActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarTitle = (WTextView) findViewById(R.id.toolbarText);
        productListWebView = (AdvancedWebView) findViewById(R.id.productListWebView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        productId = getIntent().getStringExtra("sub_category_id");
        productName = getIntent().getStringExtra("sub_category_name");
        mToolBarTitle.setText(productName);
        if (TextUtils.isEmpty(productName)) {
            url = "https://woolies.herokuapp.com/product/category/" + productId + "/products";
        } else {
            url = "https://woolies.herokuapp.com/product/category/" + productId + "/" + productName + "/products";
        }
        productListWebView.getSettings().setJavaScriptEnabled(true);
        productListWebView.loadUrl(url);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && productListWebView.canGoBack()) {
            productListWebView.goBack();
            return true;
        } else {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
