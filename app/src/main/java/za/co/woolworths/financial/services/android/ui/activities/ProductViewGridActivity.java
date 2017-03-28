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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.AddToShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.MyRunnable;
import za.co.woolworths.financial.services.android.util.PauseHandlerFragment;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductViewGridActivity extends WProductDetailActivity implements SelectedProductView,
        View.OnClickListener {
    private Toolbar mToolbar;
    private WTextView mToolBarTitle;
    private String productId;
    private String productName;
    private int pageNumber = 0;
    private RecyclerView mProductList;
    private ProductViewGridActivity mContext;
    private List<ProductList> mProduct;
    private WTextView mNumberOfItem;
    private NestedScrollView mProductScroll;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelProgressBar;
    private RelativeLayout mRelViewProgressBar;
    private ProductViewListAdapter mProductAdapter;
    private GridLayoutManager recyclerViewLayoutManager;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private ProgressBar mProgressVBar;
    private String searchItem = "";
    private int num_of_item;
    private int pageOffset;
    private PauseHandlerFragment mPauseHandlerFragment;
    private SlidingUpPanelLayout mSlideUpPanelLayout;
    public String mProductJSON;
    private LinearLayout mLinProductList;
    private boolean productCanClose = false;
    private SlidingUpPanelLayout.PanelState panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
    private Menu mMenu;
    private String mSkuId;
    private String mProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setContentView(R.layout.product_layout);
        Utils.updateStatusBarBackground(ProductViewGridActivity.this);
        mContext = this;
        initUI();
        initProductDetailUI();
        actionBar();
        bundle();

        //register pause handler
        FragmentManager fm = getSupportFragmentManager();
        String PAUSE_HANDLER_FRAGMENT_TAG = "pause_handler";
        mPauseHandlerFragment = (PauseHandlerFragment) fm.
                findFragmentByTag(PAUSE_HANDLER_FRAGMENT_TAG);
        if (mPauseHandlerFragment == null) {
            mPauseHandlerFragment = new PauseHandlerFragment();
            fm.beginTransaction()
                    .add(mPauseHandlerFragment, PAUSE_HANDLER_FRAGMENT_TAG)
                    .commit();

            hideProgressBar();
            Bundle extras = getIntent().getExtras();
            searchItem = extras.getString("searchProduct");
            String mTitle = extras.getString("title");
            String mTitleNav = extras.getString("titleNav");
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

        mSlideUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        mSlideUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0) {
                    mSlideUpPanelLayout.setAnchorPoint(1.0f);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case COLLAPSED:
                        panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
                        dismissPopWindow();
                        if (productCanClose) { //close ProductView activity when maximum row 1
                            closeGridView();
                            finish();
                        }
                        mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        break;

                    case DRAGGING:
                        if (productCanClose) { //close ProductView activity when maximum row 1
                            dismissPopWindow();
                            mLinProductList.removeAllViews();
                        }
                        break;

                    case EXPANDED:
                        panelIsCollapsed = SlidingUpPanelLayout.PanelState.EXPANDED;
                        mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        break;
                    default:
                        break;
                }
            }
        });
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
        mSlideUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLinProductList = (LinearLayout) findViewById(R.id.linProductList);
        mRelViewProgressBar = (RelativeLayout) findViewById(R.id.relViewProgressBar);
        mProductScroll = (NestedScrollView) findViewById(R.id.scrollProduct);

        mProductScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        //code to fetch more data for endless scrolling
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onSelectedProduct(View v, int position) {
        mSelectedProduct = mProduct.get(position);
        mSkuId = mSelectedProduct.otherSkus.get(0).sku;
        mProductId = mSelectedProduct.productId;
        onCallback(mProductId, mSkuId, false);
    }

    @Override
    public void onLongPressState(View v, int position) {

        ProductList mShopListProduct = mProduct.get(position);
        String productId = mShopListProduct.productId;
        String productName = mShopListProduct.productName;
        String externalImageRef = getImageByWidth(mShopListProduct.externalImageRef);
        android.app.FragmentManager fm = mContext.getFragmentManager();
        AddToShoppingListFragment mAddToShoppingListFragment =
                AddToShoppingListFragment.newInstance(productId, productName, externalImageRef);
        mAddToShoppingListFragment.show(fm, "addToShop");
    }

    @Override
    public void onSelectedColor(View v, int position) {
        selectedProduct(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ps_search_icon, menu);
        if (TextUtils.isEmpty(searchItem)) {
            menuItemVisible(menu, true);
        } else {
            menuItemVisible(menu, false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void menuItemVisible(Menu menu, boolean isVisible) {
        try {
            MenuItem menuItem = menu.findItem(R.id.action_search);
            if (isVisible) {
                menuItem.setEnabled(true);
                menuItem.getIcon().setAlpha(255);
            } else {
                menuItem.setEnabled(false);
                menuItem.getIcon().setAlpha(0);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent openSearchBarActivity = new Intent(ProductViewGridActivity.this,
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
                mProductScroll.setVisibility(View.GONE);
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
                        mProductScroll.setVisibility(View.GONE);
                        mSkuId = mProduct.get(0).otherSkus.get(0).sku;
                        mProductId = mProduct.get(0).productId;
                        onCallback(mProductId, mSkuId, true);
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
                mProductScroll.setVisibility(View.GONE);
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
                        mProductScroll.setVisibility(View.GONE);
                        mSkuId = mProduct.get(0).otherSkus.get(0).sku;
                        mProductId = mProduct.get(0).productId;
                        onCallback(mProductId, mSkuId, true);
                    } else {
                        mNumberOfItem.setText(String.valueOf(pv.pagingResponse.numItemsInTotal));
                        bindDataWithUI(mProduct);
                        mIsLastPage = false;
                        mIsLoading = false;
                        hideVProgressBar();
                    }
                } else {
                    hideVProgressBar();
                }
            }
        }.execute();
    }

    private void bindDataWithUI(List<ProductList> prod) {
        mProductAdapter = new ProductViewListAdapter(mContext, prod, mContext);
        recyclerViewLayoutManager = new GridLayoutManager(mContext, 2);
        mProductList.setLayoutManager(recyclerViewLayoutManager);
        mProductList.setNestedScrollingEnabled(false);
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
                    int actualSize = mProduct.size();
                    mProduct.addAll(moreProductList);
                    mProductAdapter.notifyItemRangeChanged(actualSize + 1, mProduct.size());
                }
                hideProgressBar();
            }
        }.execute();
    }

    private void getProductDetail(final String productId, final String skuId, final boolean closeActivity) {
        productCanClose = closeActivity;
        ((WoolworthsApplication) getApplication()).getAsyncApi().getProductDetail(productId, skuId, new Callback<String>() {
            @Override
            public void success(String strProduct, retrofit.client.Response response) {
                WProduct wProduct = Utils.stringToJson(mContext, strProduct);
                if (wProduct != null) {
                    switch (wProduct.httpCode) {
                        case 200:
                            mProductJSON = strProduct;
                            ArrayList<WProductDetail> mProductList = new ArrayList<>();
                            WProductDetail productList = wProduct.product;
                            if (productList != null) {
                                mProductList.add(productList);
                            }
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            displayProductDetail(mProductList.get(0).productName, gson.toJson(mProductList), mSkuId);
                            hideProgressDetailLoad();
                            break;

                        default:
                            Utils.updateStatusBarBackground(ProductViewGridActivity.this);
                            hideProgressDetailLoad();
                            break;
                    }

                    hideProgressDetailLoad();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProductCode();
                hideProgressDetailLoad();
            }
        });
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
        switch (panelIsCollapsed) {
            case COLLAPSED:
                finish();
                break;
            case EXPANDED:
                mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                break;
            default:
                finish();
                break;
        }
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
        menuItemVisible(mMenu, true);
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
                        .getProductSearchList(searchItem, false, pageOffset, Utils.PAGE_SIZE);
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
                    int actualSize = mProduct.size();
                    mProduct.addAll(moreProductList);
                    mProductAdapter.notifyItemRangeChanged(actualSize + 1, mProduct.size());
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

    public void onCallback(final String productId, final String skuId, final boolean closeActivity) {
        mPauseHandlerFragment.runProtected(new MyRunnable() {
            @Override
            public void run(AppCompatActivity context) {
                //this block of code should be protected from IllegalStateException
                resetProductSize();
                loadHeroImage(getImageByWidth(mSelectedProduct.externalImageRef));
                selectedColor("");
                showSlideUpView();
                setTextFromGrid();
                showPrice();
                setPromotionText("");
                resetColourField();
                showPromotionalImages(new PromotionImages());
                showPromotionalImages(mSelectedProduct.promotionImages);
                addButtonEvent();
                setIngredients("");
                resetLongDescription();
                setupPagerIndicatorDots();
                showSizeProgressBar();
                getProductDetail(productId, skuId, closeActivity);
            }
        });
    }

    private void showSlideUpView() {
        mSlideUpPanelLayout.setAnchorPoint(1.0f);
        mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        mSlideUpPanelLayout.setScrollableViewHelper(new NestedScrollableViewHelper(mScrollProductDetail));
    }

    private void closeGridView() {
        if (productCanClose) {
            mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mLinProductList.removeAllViews();
        }
    }
}

