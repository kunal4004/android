package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WObservableScrollView;
import za.co.woolworths.financial.services.android.ui.views.WProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Const;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
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
    private final int PAGE_SIZE = 30;
    private final int PERMS_REQUEST_CODE = 1234;
    private LatLng mLocation;
    private int mCurrentPage = 1;
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
    private WProgressDialogFragment mGetProgressDialog;
    private String searchItem = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Utils.updateStatusBarBackground(ProductViewActivity.this);
        mContext = this;
        initUI();
        actionBar();
        bundle();
        if (hasPermissions()) {
            startLocationUpdate();
        } else {
            requestPerms();
        }
        fm = getSupportFragmentManager();
        mGetProgressDialog = WProgressDialogFragment.newInstance("v");
        mGetProgressDialog.setCancelable(false);
        hideProgressBar();
        Bundle extras = getIntent().getExtras();
        searchItem = extras.getString("searchProduct");
        if (TextUtils.isEmpty(searchItem)) {
            productConfig(productName);
            searchItem = "";
            loadProduct();
        } else {
            productConfig(searchItem);
            searchProduct();
        }
    }

    private void productConfig(String productName) {
        mLocation = new LatLng(0, 0);
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
            mActionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.appbar_background));
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

    public boolean hasPermissions() {
        int res;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void startLocationUpdate() {
        // start location updates
        FusedLocationSingleton.getInstance().startLocationUpdates();
        // register observer for location updates
        LocalBroadcastManager.getInstance(ProductViewActivity.this).registerReceiver(mLocationUpdated,
                new IntentFilter(Const.INTENT_FILTER_LOCATION_UPDATE));
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionIsAllowed = true;
        switch (requestCode) {
            case PERMS_REQUEST_CODE:
                for (int res : grantResults) {
                    // if user granted all permissions.
                    permissionIsAllowed = permissionIsAllowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // if user not granted permissions.
                permissionIsAllowed = false;
                break;
        }
        if (permissionIsAllowed) {
            //user granted all permissions we can perform our task.
            startLocationUpdate();
            loadProduct();
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Location Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop location updates
        try {
            FusedLocationSingleton.getInstance().stopLocationUpdates();
            // unregister observer
            LocalBroadcastManager.getInstance(ProductViewActivity.this).unregisterReceiver(mLocationUpdated);
        } catch (NullPointerException ex) {
            Log.e("onPauseFusedLoc", ex.toString());
        }
    }

    /***********************************************************************************************
     * local broadcast receiver
     **********************************************************************************************/
    /**
     * handle new location
     */
    private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Location location = intent.getParcelableExtra(Const.LBM_EVENT_LOCATION_UPDATE);
                mLocation = new LatLng(location.getLatitude(), location.getLongitude());
            } catch (NullPointerException e) {
                mLocation = new LatLng(0, 0);
            }
        }
    };

    @Override
    public void onSelectedProduct(View v, int position) {
        try {
            getProductDetail(mProduct.get(position).productId, mProduct.get(position).otherSkus.get(0).sku);
        } catch (Exception ex) {
        }

    }

    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
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
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreProduct();
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        try {
            switch (scrollState) {
                case UP:
                    hideViews();
                    break;
                case DOWN:
                    showViews();
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
        }
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
                Intent openSearchBarActivity = new Intent(ProductViewActivity.this, ProductSearchActivity.class);
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
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
    }

    public void hideProgressBar() {
        mRelProgressBar.setVisibility(View.GONE);
    }


    public void loadProduct() {

        new HttpAsyncTask<String, String, ProductView>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showVProgressBar();
            }

            @Override
            protected ProductView httpDoInBackground(String... params) {
                mCurrentPage = 1;
                mIsLastPage = false;
                return ((WoolworthsApplication) getApplication()).getApi().productViewRequest(mLocation, false,
                        mCurrentPage, PAGE_SIZE, productId);
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
                    mNumberOfItem.setText(String.valueOf(pv.pagingResponse.numItemsInTotal));
                    bindDataWithUI(mProduct);
                    mIsLastPage = false;
                    mCurrentPage = 1;
                    mIsLoading = false;
                }
                hideVProgressBar();
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
                mCurrentPage = 1;
                mIsLastPage = false;

                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(searchItem,
                                mLocation, false, mCurrentPage, PAGE_SIZE);
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
                    mNumberOfItem.setText(String.valueOf(pv.pagingResponse.numItemsInTotal));
                    bindDataWithUI(mProduct);
                    mIsLastPage = false;
                    mCurrentPage = 1;
                    mIsLoading = false;
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
                mCurrentPage += 1;
            }

            @Override
            protected ProductView httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().productViewRequest(mLocation, false,
                        mCurrentPage, PAGE_SIZE, productId);
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
                List<ProductList> moreProductList = null;
                moreProductList = new ArrayList<>();
                moreProductList = productResponse.products;
                if (moreProductList != null && moreProductList.size() != 0) {
                    if (moreProductList.size() < PAGE_SIZE) {
                        mIsLastPage = true;
                    }
                    mProduct.addAll(moreProductList);
                    mProductAdapter.notifyDataSetChanged();
                }
                hideProgressBar();
            }
        }.execute();
    }

    private void getProductDetail(final String productId, final String skuId) {
        new HttpAsyncTask<String, String, WProduct>() {
            @Override
            protected WProduct httpDoInBackground(String... params) {
                WProduct product = ((WoolworthsApplication) getApplication()).getApi().getProductDetailView(productId, skuId);
                return product;
            }

            @Override
            protected WProduct httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                dismissFragmentDialog();
                return new WProduct();
            }

            @Override
            protected Class<WProduct> httpDoInBackgroundReturnType() {
                return WProduct.class;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    mGetProgressDialog.show(fm, "v");
                } catch (NullPointerException ignored) {
                }
            }

            @Override
            protected void onPostExecute(WProduct product) {
                super.onPostExecute(product);
                ArrayList<WProductDetail> mProductList = null;
                WProductDetail productList = product.product;
                mProductList = new ArrayList<>();
                if (productList != null) {
                    mProductList.add(productList);
                }
                if (productList != null) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Intent openDetailView = new Intent(mContext, ProductDetailViewActivity.class);
                    openDetailView.putExtra("product_name", mProductList.get(0).productName);
                    openDetailView.putExtra("product_detail", gson.toJson(mProductList));
                    startActivity(openDetailView);
                    overridePendingTransition(0, R.anim.anim_slide_up);
                }
                dismissFragmentDialog();
            }
        }.execute();
    }

    private void dismissFragmentDialog() {
        if (mGetProgressDialog != null) {
            if (mGetProgressDialog.isVisible()) {
                mGetProgressDialog.dismiss();
            }
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


}
