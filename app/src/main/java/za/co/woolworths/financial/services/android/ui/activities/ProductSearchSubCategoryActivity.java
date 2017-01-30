package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.adapters.PSSubCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.views.WObservableRecyclerView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.binder.view.SubCategoryBinder;

public class ProductSearchSubCategoryActivity extends AppCompatActivity implements View.OnClickListener,
        SubCategoryBinder.OnClickListener, ObservableScrollViewCallbacks {

    private Toolbar mToolbar;
    private WObservableRecyclerView recyclerView;
    private SearchView searchView;
    private ConnectionDetector mConnectionDetector;
    private LayoutInflater mLayoutInflater;
    private List<SubCategory> mSubCategories;
    private LinearLayoutManager mLayoutManager;
    private PSSubCategoryAdapter mPSRootCategoryAdapter;
    private ProductSearchSubCategoryActivity mContext;
    private WTextView mTextNoProductFound;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WTextView mToolBarTitle;
    private int mCatStep;
    private String mRootCategoryName;
    private String mRootCategoryId;
    private String mSubCategoriesName;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_search_sub_category);
        mContext = this;
        statusBar();

        Bundle bundleSubCategory = getIntent().getExtras();
        if (bundleSubCategory != null) {
            mRootCategoryId = bundleSubCategory.getString("root_category_id");
            mRootCategoryName = bundleSubCategory.getString("root_category_name");
            mCatStep = bundleSubCategory.getInt("catStep");
            mSubCategoriesName = bundleSubCategory.getString("sub_category_name");
        }
        mConnectionDetector = new ConnectionDetector();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        initUI();
        loadData();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                if (mConnectionDetector.isOnline(ProductSearchSubCategoryActivity.this)) {
                    loadData();
                } else {
//                    mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
//                            SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                    hideRefreshView();
                }
            }
        });
    }

    private void initUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarTitle = (WTextView) findViewById(R.id.toolbarText);
        recyclerView = (WObservableRecyclerView) findViewById(R.id.productSearchList);
        LinearLayout recentSearchLayout = (LinearLayout) findViewById(R.id.recentSearchLayout);
        LinearLayout recentSearchList = (LinearLayout) findViewById(R.id.recentSearchList);
        mTextNoProductFound = (WTextView) findViewById(R.id.textNoProductFound);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setScrollViewCallbacks(this);
       // setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (mCatStep == 0)
            mToolBarTitle.setText(mRootCategoryName);
        else
            mToolBarTitle.setText(mSubCategoriesName);

    }

    private void statusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    public void onClick(View v) {
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
                Intent openSearchBarActivity = new Intent(ProductSearchSubCategoryActivity.this, ProductSearchActivity.class);
                startActivity(openSearchBarActivity);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                break;
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getSubCategoryRequest(final String categeory_id) {
        if (mConnectionDetector.isOnline(ProductSearchSubCategoryActivity.this)) {
            new HttpAsyncTask<String, String, SubCategories>() {
                @Override
                protected SubCategories httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) getApplication()).getApi().getSubCategory(categeory_id);
                }

                @Override
                protected SubCategories httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    SubCategories subCategories = new SubCategories();
                    hideRefreshView();
                    return subCategories;
                }

                @Override
                protected Class<SubCategories> httpDoInBackgroundReturnType() {
                    return SubCategories.class;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(SubCategories subCategories) {
                    super.onPostExecute(subCategories);

                    switch (subCategories.httpCode) {
                        case 200:
                            if (subCategories.subCategories != null && subCategories.subCategories.size() != 0) {
                                mSubCategories = subCategories.subCategories;
                                mPSRootCategoryAdapter = new PSSubCategoryAdapter(subCategories.subCategories, mContext);
                                mLayoutManager = new LinearLayoutManager(ProductSearchSubCategoryActivity.this);
                                mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                recyclerView.setLayoutManager(mLayoutManager);
                                recyclerView.setNestedScrollingEnabled(false);
                                recyclerView.setAdapter(mPSRootCategoryAdapter);
                                mPSRootCategoryAdapter.setCLIContent();
                                hideNoProductFound();
                            } else {
                                showNoProductFound();
                            }

                            break;

                        default:
//                            mSlidingUpViewLayout.openOverlayView(subCategories.response.desc,
//                                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                            break;
                    }
                    hideRefreshView();
                }

            }.execute();
        } else {
//            mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
//                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
        }
    }

    private void showNoProductFound() {
        mTextNoProductFound.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideNoProductFound() {
        mTextNoProductFound.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v, final int position) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SubCategory subCategory = mSubCategories.get(position);
                Intent openProductCategory = new Intent(ProductSearchSubCategoryActivity.this, ProductSearchSubCategoryActivity.class);
                openProductCategory.putExtra("root_category_id", subCategory.categoryId);
                openProductCategory.putExtra("sub_category_name", subCategory.categoryName);
                openProductCategory.putExtra("catStep", 1);
                startActivity(openProductCategory);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, 200);
    }

    public void hideRefreshView() {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void loadData() {
        if (mConnectionDetector.isOnline(ProductSearchSubCategoryActivity.this)) {
            getSubCategoryRequest(mRootCategoryId);
        }
    }
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = getSupportActionBar();
        if (ab == null) {
            return;
        }
        if (scrollState == ScrollState.UP) {

            mToolbar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();

        } else if (scrollState == ScrollState.DOWN) {
            mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        }
    }
}
