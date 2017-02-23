package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.AddToShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.views.ProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WObservableScrollView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductViewActivity extends AppCompatActivity implements SelectedProductView,
        ObservableScrollViewCallbacks {
    private Toolbar mToolbar;
    private WTextView mToolBarTitle;
    private String productId;
    private String productName;
    private int pageNumber = 0;
    private RecyclerView mProductList;
    private ProductViewActivity mContext;
    private List<ProductList> mProduct;
    private WTextView mNumberOfItem;
    private WObservableScrollView mProductScroll;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelProgressBar;
    private RelativeLayout mRelViewProgressBar;
    private ProductViewListAdapter mProductAdapter;
    private GridLayoutManager recyclerViewLayoutManager;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private int mScrollY = 0;
    private ProgressBar mProgressVBar;
    private FragmentManager fm;
    private String searchItem = "";
    private String mTitle;
    private String mTitleNav;
    private int num_of_item;
    private int pageOffset;
    private ProgressDialogFragment mProgressDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Utils.updateStatusBarBackground(ProductViewActivity.this);
        mContext = this;
        initUI();
        actionBar();
        bundle();
        fm = getSupportFragmentManager();
        hideProgressBar();
        Bundle extras = getIntent().getExtras();
        searchItem = extras.getString("searchProduct");
        mTitle = extras.getString("title");
        mTitleNav = extras.getString("titleNav");

        if (TextUtils.isEmpty(searchItem)) {
            if (!TextUtils.isEmpty(mTitle)) {
                productName = mTitleNav;
                productId = mTitle;
            }
            productConfig(productName);
            searchItem = "";
            loadProduct();
        } else {
            if (TextUtils.isEmpty(mTitle)) {
                productConfig(searchItem);
            } else {
                productConfig(mTitle);
            }

            productId = searchItem;
            searchProduct();
        }

        registerReceiver(broadcast_reciever, new IntentFilter("closeProductView"));
    }

    private void productConfig(String productName) {
        mToolBarTitle.setText(productName);
    }

    private void bundle() {
        productName = getIntent().getStringExtra("sub_category_name");
        productId = getIntent().getStringExtra("sub_category_id");
    }

    private void actionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setBackgroundDrawable(ContextCompat.getDrawable(this,
                    R.drawable.appbar_background));
        }
    }

    private void initUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarTitle = (WTextView) findViewById(R.id.toolbarText);
        mNumberOfItem = (WTextView) findViewById(R.id.numberOfItem);
        mProductList = (RecyclerView) findViewById(R.id.productList);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        mProgressVBar = (ProgressBar) findViewById(R.id.mProgressB);
        mRelProgressBar = (RelativeLayout) findViewById(R.id.relProgressBar);
        mRelViewProgressBar = (RelativeLayout) findViewById(R.id.relViewProgressBar);
        mProductScroll = (WObservableScrollView) findViewById(R.id.scrollProduct);
        mProductScroll.setScrollViewCallbacks(this);
    }

    @Override
    public void onSelectedProduct(View v, int position) {
        try {
            getProductDetail(mProduct.get(position).productId,
                    mProduct.get(position).otherSkus.get(0).sku, false);
        } catch (Exception ex) {
            Log.e("ExceptionProduct", ex.toString());
        }
    }

    @Override
    public void onLongPressState(View v, int position) {
        String productId = mProduct.get(position).productId;
        String productName = mProduct.get(position).productName;
        String externalImageRef = mProduct.get(position).externalImageRef;
        android.app.FragmentManager fm = mContext.getFragmentManager();
        AddToShoppingListFragment mAddToShoppingListFragment =
                AddToShoppingListFragment.newInstance(productId, productName, externalImageRef);
        mAddToShoppingListFragment.show(fm, "addToShop");
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        this.mScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {
        // calculates how much scrollview will scroll
        int scrollingHeight = mProductScroll.getChildAt(0).getHeight() - mProductScroll.getHeight();
        if (scrollingHeight <= mScrollY) {
            //scroll reached bottom
            try {
                int visibleItemCount = recyclerViewLayoutManager.getChildCount();
                int totalItemCount = recyclerViewLayoutManager.getItemCount();
                int firstVisibleItemPosition = recyclerViewLayoutManager.findFirstVisibleItemPosition();
                if (!mIsLoading && !mIsLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= Utils.PAGE_SIZE) {
                        if (mProduct.size() < num_of_item) {
                            if (TextUtils.isEmpty(searchItem)) {
                                loadMoreProduct();
                            } else {
                                searchMoreProduct();
                            }
                        }
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ps_search_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent openSearchBarActivity = new Intent(ProductViewActivity.this,
                        ProductSearchActivity.class);
                startActivity(openSearchBarActivity);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgressBar() {
        mRelProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK,
                PorterDuff.Mode.MULTIPLY);
    }

    public void hideProgressBar() {
        mRelProgressBar.setVisibility(View.GONE);
    }


    public void loadProduct() {
        mNumberOfItem.setText(String.valueOf(0));
        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showVProgressBar();
            }

            @Override
            protected ProductView httpDoInBackground(String... params) {
                pageNumber = 0;
                mIsLastPage = false;
                return ((WoolworthsApplication) getApplication()).getApi().productViewRequest(false,
                        pageNumber, Utils.PAGE_SIZE, productId);

            }

            @Override
            protected Class<ProductView> httpDoInBackgroundReturnType() {
                return ProductView.class;
            }

            @Override
            protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                ProductView productResponse = new ProductView();
                productResponse.response = new Response();
                hideVProgressBar();
                return productResponse;
            }

            @Override
            protected void onPostExecute(ProductView pv) {
                super.onPostExecute(pv);
                mProduct = null;
                mProduct = new ArrayList<>();
                if (pv.products != null && pv.products.size() != 0) {
                    mProduct = pv.products;
                    if (pv.products.size() == 1) {
                        getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku, true);
                    } else {
                        num_of_item = pv.pagingResponse.numItemsInTotal;
                        mNumberOfItem.setText(String.valueOf(num_of_item));
                        bindDataWithUI(mProduct);
                        mIsLastPage = false;
                        pageNumber = 0;
                        mIsLoading = false;
                        hideVProgressBar();
                    }
                } else {
                    mNumberOfItem.setText(String.valueOf(0));
                    hideVProgressBar();
                }
            }
        }.execute();
    }

    public void searchProduct() {

        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showVProgressBar();
            }

            @Override
            protected ProductView httpDoInBackground(String... params) {
                pageNumber = 0;
                mIsLastPage = false;

                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(searchItem, false, pageNumber, Utils.PAGE_SIZE);

            }

            @Override
            protected Class<ProductView> httpDoInBackgroundReturnType() {
                return ProductView.class;
            }

            @Override
            protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                ProductView productResponse = new ProductView();
                productResponse.response = new Response();
                hideVProgressBar();
                return productResponse;
            }

            @Override
            protected void onPostExecute(ProductView pv) {
                super.onPostExecute(pv);
                mProduct = null;
                mProduct = new ArrayList<>();
                if (pv.products != null && pv.products.size() != 0) {
                    mProduct = pv.products;
                    num_of_item = pv.pagingResponse.numItemsInTotal;

                    if (pv.products.size() == 1) {
                        getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku, true);
                    } else {
                        mNumberOfItem.setText(String.valueOf(pv.pagingResponse.numItemsInTotal));
                        bindDataWithUI(mProduct);
                        mIsLastPage = false;
                        mIsLoading = false;
                    }
                }
                hideVProgressBar();
            }
        }.execute();
    }

    private void bindDataWithUI(List<ProductList> prod) {
        mProductAdapter = new ProductViewListAdapter(mContext, prod, mContext);
        recyclerViewLayoutManager = new GridLayoutManager(mContext, 2);
        mProductList.setLayoutManager(recyclerViewLayoutManager);
        mProductList.setAdapter(mProductAdapter);
    }

    public void loadMoreProduct() {
        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressBar();
                mIsLoading = true;
                pageNumber += 1;
                pagination();
            }

            @Override
            protected ProductView httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().productViewRequest(false,
                        pageOffset, Utils.PAGE_SIZE, productId);
            }

            @Override
            protected Class<ProductView> httpDoInBackgroundReturnType() {
                return ProductView.class;
            }

            @Override
            protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                ProductView productResponse = new ProductView();
                productResponse.response = new Response();
                hideProgressBar();
                mIsLoading = false;
                return productResponse;
            }

            @Override
            protected void onPostExecute(ProductView productResponse) {
                super.onPostExecute(productResponse);
                mIsLoading = false;
                List<ProductList> moreProductList;
                moreProductList = productResponse.products;
                if (moreProductList != null && moreProductList.size() != 0) {
                    if (moreProductList.size() < Utils.PAGE_SIZE) {
                        mIsLastPage = true;
                    }
                    mProduct.addAll(moreProductList);
                    mProductAdapter.notifyDataSetChanged();
                }
                hideProgressBar();
            }
        }.execute();
    }

    private void getProductDetail(final String productId, final String skuId, final boolean closeActivity) {
        try {
            if (!mProgressDialogFragment.isAdded()) {
                mProgressDialogFragment = ProgressDialogFragment.newInstance();
                mProgressDialogFragment.show(fm, "v");
            } else {
                mProgressDialogFragment.dismiss();
                mProgressDialogFragment = ProgressDialogFragment.newInstance();
                mProgressDialogFragment.show(fm, "v");
            }
        } catch (Exception ignored) {
        }
        ((WoolworthsApplication) getApplication()).getAsyncApi().getProductDetail(productId, skuId, new Callback<String>() {
            @Override
            public void success(String strProduct, retrofit.client.Response response) {
                WProduct wProduct = Utils.stringToJson(mContext, strProduct);
                if (wProduct != null) {
                    switch (wProduct.httpCode) {
                        case 200:
                            ArrayList<WProductDetail> mProductList;
                            WProductDetail productList = wProduct.product;
                            mProductList = new ArrayList<>();
                            if (productList != null) {
                                mProductList.add(productList);
                            }
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            Intent openDetailView = new Intent(mContext, ProductDetailViewActivity.class);
                            openDetailView.putExtra("product_name", mProductList.get(0).productName);
                            openDetailView.putExtra("product_detail", gson.toJson(mProductList));
                            startActivity(openDetailView);
                            overridePendingTransition(R.anim.slide_down, R.anim.anim_slide_up);
                            if (closeActivity) { //close ProductView activity when 1 row exist
                                finish();
                            }
                            break;

                        default:
                            dismissFragmentDialog();
                            break;
                    }
                }

                dismissFragmentDialog();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("StringValuexx", error.toString());
                dismissFragmentDialog();
            }
        });
    }

    private void dismissFragmentDialog() {
        try {
            if (mProgressDialogFragment != null) {
                if (mProgressDialogFragment.isVisible()) {
                    mProgressDialogFragment.dismiss();
                }
            }
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void hideVProgressBar() {
        mRelViewProgressBar.setVisibility(View.GONE);
        mProductScroll.setVisibility(View.VISIBLE);
        mProgressVBar.getIndeterminateDrawable().setColorFilter(null);
    }

    private void showVProgressBar() {
        mRelViewProgressBar.setVisibility(View.VISIBLE);
        mProductScroll.setVisibility(View.GONE);
        mProgressVBar.getIndeterminateDrawable().setColorFilter(null);
        mProgressVBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionExit();
    }

    BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast_reciever);
    }


    /***
     * LOAD MORE PRODUCT FROM SEARCH
     ***/

    public void searchMoreProduct() {
        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressBar();
                mIsLoading = true;
                pageNumber += 1;
                pagination();
            }

            @Override
            protected ProductView httpDoInBackground(String... params) {
                mIsLastPage = false;
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(searchItem, false, pageNumber, Utils.PAGE_SIZE);
            }

            @Override
            protected Class<ProductView> httpDoInBackgroundReturnType() {
                return ProductView.class;
            }

            @Override
            protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                ProductView productResponse = new ProductView();
                productResponse.response = new Response();
                hideProgressBar();
                mIsLoading = false;
                return productResponse;
            }

            @Override
            protected void onPostExecute(ProductView pv) {
                super.onPostExecute(pv);
                mIsLoading = false;
                List<ProductList> moreProductList;
                moreProductList = pv.products;
                if (moreProductList != null && moreProductList.size() != 0) {
                    if (moreProductList.size() < Utils.PAGE_SIZE) {
                        mIsLastPage = true;
                    }
                    mProduct.addAll(moreProductList);
                    mProductAdapter.notifyDataSetChanged();
                }
                hideProgressBar();
            }
        }.execute();
    }

    private void pagination() {
        if (mProduct.size() < num_of_item) {
            if (pageNumber == 1) {
                pageOffset = Utils.PAGE_SIZE + 1;
            } else {
                pageOffset = pageOffset + Utils.PAGE_SIZE;
            }
        }
    }
}
