package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.adapters.PSSubCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.views.WObservableRecyclerView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.SubCategoryBinder;

public class ProductSearchSubCategoryActivity extends BaseActivity implements View.OnClickListener,
        SubCategoryBinder.OnClickListener, ObservableScrollViewCallbacks {

    private Toolbar mToolbar;
    private WObservableRecyclerView recyclerView;
    private ConnectionDetector mConnectionDetector;
    private List<SubCategory> mSubCategories;
    private LinearLayoutManager mLayoutManager;
    private PSSubCategoryAdapter mPSRootCategoryAdapter;
    private ProductSearchSubCategoryActivity mContext;
    private WTextView mTextNoProductFound;
    private int mCatStep;
    private String mRootCategoryName;
    private String mRootCategoryId;
    private String mSubCategoriesName;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private ProgressBar mProgressBar;
    private RelativeLayout mSearchStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.product_search_sub_category);
        mContext = this;
        Bundle bundleSubCategory = getIntent().getExtras();
        if (bundleSubCategory != null) {
            mRootCategoryId = bundleSubCategory.getString("root_category_id");
            mRootCategoryName = bundleSubCategory.getString("root_category_name");
            mCatStep = bundleSubCategory.getInt("catStep");
            mSubCategoriesName = bundleSubCategory.getString("sub_category_name");
        }
        mConnectionDetector = new ConnectionDetector();
        mPopWindowValidationMessage = new PopWindowValidationMessage(this);
        initUI();
        loadData();
    }

    private void initUI() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        WTextView mToolBarTitle = (WTextView) findViewById(R.id.toolbarText);
        recyclerView = (WObservableRecyclerView) findViewById(R.id.productSearchList);
        mTextNoProductFound = (WTextView) findViewById(R.id.textNoProductFound);
        ImageView mImBurgerButtonPressed = (ImageView) findViewById(R.id.imBurgerButtonPressed);
        mSearchStore = (RelativeLayout) findViewById(R.id.search_store_activity);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        ImageView mImSearch = (ImageView) findViewById(R.id.imSearch);

        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setScrollViewCallbacks(this);
        mImSearch.setOnClickListener(this);
        mImBurgerButtonPressed.setOnClickListener(this);
        if (mCatStep == 0)
            mToolBarTitle.setText(mRootCategoryName);
        else
            mToolBarTitle.setText(mSubCategoriesName);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imBurgerButtonPressed:
                onBackPressed();
                break;
            case R.id.imSearch:
                Intent openSearchActivity = new Intent(this, ProductSearchActivity.class);
                startActivity(openSearchActivity);
                break;
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
                Intent openSearchBarActivity = new Intent(ProductSearchSubCategoryActivity.this, ProductSearchActivity.class);
                startActivity(openSearchBarActivity);
                break;
            case android.R.id.home:
                onBackPressed();
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
                    hideProgressBar();
                    //  hideRefreshView();
                    return subCategories;
                }

                @Override
                protected Class<SubCategories> httpDoInBackgroundReturnType() {
                    return SubCategories.class;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    showProgressBar();
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
                                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
                                recyclerView.setNestedScrollingEnabled(false);
                                recyclerView.setAdapter(mPSRootCategoryAdapter);
                                mPSRootCategoryAdapter.setCLIContent();
                                hideNoProductFound();
                            } else {
                                showNoProductFound();
                            }

                            hideProgressBar();
                            break;

                        default:
                            mPopWindowValidationMessage.displayValidationMessage(subCategories.response.desc,
                                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                            break;
                    }
                    mProgressBar.setVisibility(View.GONE);
                    //  hideRefreshView();
                }

            }.execute();
        } else {
            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.connect_to_server),
                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
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

                if (subCategory.hasChildren) {
                    Intent openProductCategory = new Intent(ProductSearchSubCategoryActivity.this, ProductSearchSubCategoryActivity.class);
                    openProductCategory.putExtra("root_category_id", subCategory.categoryId);
                    openProductCategory.putExtra("sub_category_name", subCategory.categoryName);
                    openProductCategory.putExtra("catStep", 1);
                    startActivity(openProductCategory);
                } else {
                    Intent openProductListIntent = new Intent(ProductSearchSubCategoryActivity.this, ProductViewGridActivity.class);
                    openProductListIntent.putExtra("sub_category_name", subCategory.categoryName);
                    openProductListIntent.putExtra("sub_category_id", subCategory.categoryId);
                    startActivity(openProductListIntent);
                }
            }
        }, 200);
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
        if (scrollState == ScrollState.UP) {
            mToolbar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
            mSearchStore.setBackgroundColor(Color.WHITE);
        } else if (scrollState == ScrollState.DOWN) {
            mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
            mSearchStore.setBackgroundColor(Color.WHITE);
        }
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(null);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
    }
}
