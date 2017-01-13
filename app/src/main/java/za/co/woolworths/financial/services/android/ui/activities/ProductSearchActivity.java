package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;
import com.daimajia.swipe.util.Attributes;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Product;
import za.co.woolworths.financial.services.android.models.dto.Product_;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.adapters.ProductListAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductSearchActivity extends AppCompatActivity {
    public RecyclerView productListview;
    public LinearLayoutManager mLayoutManager;
    public Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    public static final int PAGE_SIZE = 20;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private int mCurrentPage = 1;
    private ConnectionDetector  connectionDetector;
    private SharePreferenceHelper mSharePreferenceHelper;
    private LayoutInflater mLayoutInflater;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    private ArrayList<Product_> productList;
    private ProductListAdapter mProductListAdapter;
    private List<Product_> moreProductList;
    private String searchProductBrand;
    private WTextView mToolbarText;
    private WTextView mTextNoProductFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_search_activity);
        Utils.updateStatusBarBackground(this);
        setActionBar();
        initUI();
        connectionDetector = new ConnectionDetector();
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(this);
        mLayoutInflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mSlidingUpViewLayout = new SlidingUpViewLayout(this,mLayoutInflater);
        recycleview();
        scrollListener();
    }

    private void initUI() {
        mLayoutManager = new LinearLayoutManager(ProductSearchActivity.this);
        productListview = (RecyclerView) findViewById(R.id.productSearchList);
        mToolbarText = (WTextView) findViewById(R.id.toolbarText);
        mTextNoProductFound = (WTextView) findViewById(R.id.textNoProductFound);
    }

    private void setActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    private void scrollListener() {
        productListview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if (!mIsLoading && !mIsLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreMessages(searchProductBrand);
                    }
                }
            }
        });

        if (connectionDetector.isOnline(ProductSearchActivity.this)) {
            loadMessages(searchProductBrand);
        }else {
            mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
        }
    }

    private void recycleview() {
        searchProductBrand = mSharePreferenceHelper.getValue("search_prod_brand");
        mToolbarText.setText(searchProductBrand);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        productListview.setHasFixedSize(true);
        productListview.setLayoutManager(mLayoutManager);
        mToolbarText.setText(searchProductBrand);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if(connectionDetector.isOnline(ProductSearchActivity.this)){
                    loadMessages(searchProductBrand);
                }else {
                    mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                            SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                    hideRefreshView();
                }
            }
        });
    }


    private void hideRefreshView() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void loadMessages(final String query) {
            new HttpAsyncTask<String, String, Product>() {
                @Override
                protected Product httpDoInBackground(String... params) {
                    Location location=Utils.getLastSavedLocation(ProductSearchActivity.this);
                    LatLng location1 = new LatLng(-29.79,31.0833);
                    return ((WoolworthsApplication) getApplication()).getApi()
                            .getProductSearchList(query,
                                    location1,false,mCurrentPage,PAGE_SIZE);
                }

                @Override
                protected Product httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    Product product = new Product();
                    return product;
                }

                @Override
                protected void onPostExecute(Product product) {
                    super.onPostExecute(product);
                    productList = null;
                    productList = new ArrayList<>();
                    productList = product.products;
                    if (productList != null) {
                        if (product.products.size() > 0) {
                            bindDataWithUI(productList);
                            mIsLastPage = false;
                            mCurrentPage = 1;
                            mIsLoading = false;
                            hideNoProductFound();
                        }else {
                            showNoProductFound();
                        }
                    }else{
                        showNoProductFound();
                    }
                    hideRefreshView();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Class<Product> httpDoInBackgroundReturnType() {
                    return Product.class;
                }
            }.execute();
    }

    public void bindDataWithUI(List<Product_> product_list) {
        mProductListAdapter = new ProductListAdapter(ProductSearchActivity.this, product_list);
        ((ProductListAdapter) mProductListAdapter).setMode(Attributes.Mode.Single);
        productListview.setAdapter(mProductListAdapter);
    }

    public void loadMoreMessages(final String query) {
        new HttpAsyncTask<String, String, Product>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mIsLoading = true;
                mCurrentPage += 1;
            }

            @Override
            protected Product httpDoInBackground(String... params) {
                Location loc=Utils.getLastSavedLocation(ProductSearchActivity.this);
                LatLng location = new LatLng(-29.79,31.0833);
                return ((WoolworthsApplication) getApplication()).getApi()
                        .getProductSearchList(query,
                                location,false,mCurrentPage,PAGE_SIZE);
            }

            @Override
            protected Class<Product> httpDoInBackgroundReturnType() {
                return Product.class;
            }

            @Override
            protected Product httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                Product productResponse = new Product();
                productResponse.response = new Response();
                mIsLoading = false;
                return productResponse;
            }

            @Override
            protected void onPostExecute(Product productResponse) {
                super.onPostExecute(productResponse);
                mIsLoading = false;
                List<Product_> product_ = null;
                product_ = new ArrayList<>();
                moreProductList = productResponse.products;
                if (moreProductList != null && moreProductList.size() != 0) {
                    if (moreProductList.size() < PAGE_SIZE) {
                        mIsLastPage = true;
                    }
                    productList.addAll(moreProductList);
                    mProductListAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return  true;
        }
        return false;
    }

    private void showNoProductFound() {
        mTextNoProductFound.setVisibility(View.VISIBLE);
        productListview.setVisibility(View.GONE);
    }

    private void hideNoProductFound() {
        mTextNoProductFound.setVisibility(View.GONE);
        productListview.setVisibility(View.VISIBLE);
    }
}
