package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.SubCategories;
import za.co.woolworths.financial.services.android.models.dto.SubCategory;
import za.co.woolworths.financial.services.android.ui.adapters.PSSubCategoryAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.binder.view.SubCategoryBinder;

public class ProductSearchSubCategoryActivity extends AppCompatActivity implements View.OnClickListener, SubCategoryBinder.OnClickListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LinearLayout recentSearchLayout;
    private LinearLayout recentSearchList;
    private SharePreferenceHelper mSharePreferenceHelper;
    private ConnectionDetector mConnectionDetector;
    private LayoutInflater mLayoutInflater;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    private List<SubCategory> mSubCategories;
    private LinearLayoutManager mLayoutManager;
    private PSSubCategoryAdapter mPSRootCategoryAdapter;
    private ProductSearchSubCategoryActivity mContext;
    private WTextView mTextNoProductFound;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WTextView mToolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_search_activity);
        mContext = this;
        statusBar();
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(this);
        mConnectionDetector = new ConnectionDetector();
        mLayoutInflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mSlidingUpViewLayout = new SlidingUpViewLayout(this,mLayoutInflater);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        initUI();

        loadData();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                if(mConnectionDetector.isOnline(ProductSearchSubCategoryActivity.this)){
                    loadData();
                }else {
                    mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                            SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                    hideRefreshView();
                }
            }
        });
    }

    private void initUI() {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        mToolBarTitle =(WTextView)findViewById(R.id.toolbarText);
        recyclerView=(RecyclerView)findViewById(R.id.productSearchList) ;
        recentSearchLayout=(LinearLayout)findViewById(R.id.recentSearchLayout);
        recentSearchList=(LinearLayout)findViewById(R.id.recentSearchList);
        mTextNoProductFound =(WTextView)findViewById(R.id.textNoProductFound);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (mSharePreferenceHelper.getValue("catStep").equalsIgnoreCase("0"))
            mToolBarTitle.setText(mSharePreferenceHelper.getValue("root_category_name"));
        else
            mToolBarTitle.setText(mSharePreferenceHelper.getValue("sub_category_name"));

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
       // MenuItem searchViewItem = menu.findItem(R.id.action_search);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getSubCategoryRequest(final String categeory_id){
        if(mConnectionDetector.isOnline()) {
            new HttpAsyncTask<String, String, SubCategories>() {
                @Override
                protected SubCategories httpDoInBackground(String... params) {
                    return ((WoolworthsApplication)getApplication()).getApi().getSubCategory(categeory_id);
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
                            if (subCategories.subCategories != null&&subCategories.subCategories.size()!=0) {
                                mSubCategories = subCategories.subCategories;
                                mPSRootCategoryAdapter = new PSSubCategoryAdapter(subCategories.subCategories, mContext);
                                mLayoutManager = new LinearLayoutManager(ProductSearchSubCategoryActivity.this);
                                mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                recyclerView.setLayoutManager(mLayoutManager);
                                recyclerView.setNestedScrollingEnabled(false);
                                recyclerView.setAdapter(mPSRootCategoryAdapter);
                                mPSRootCategoryAdapter.setCLIContent();
                                hideNoProductFound();
                            }else{
                                showNoProductFound();
                            }
                            break;

                        default:
                            mSlidingUpViewLayout.openOverlayView(subCategories.response.desc,
                                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                            break;
                    }
                    hideRefreshView();
                }

            }.execute();
        }else {
            mSlidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server),
                    SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
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
    public void onClick(View v, int position) {
        SubCategory subCategory = mSubCategories.get(position);
        mSharePreferenceHelper.save(subCategory.categoryId,"sub_category_id");
        mSharePreferenceHelper.save(subCategory.categoryName,"sub_category_name");
        mSharePreferenceHelper.save("1","catStep");
        Intent openProductCategory = new Intent(ProductSearchSubCategoryActivity.this,ProductSearchSubCategoryActivity.class);
        startActivity(openProductCategory);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void hideRefreshView() {
        if (mSwipeRefreshLayout!=null&&mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void loadData() {
        if (mConnectionDetector.isOnline(ProductSearchSubCategoryActivity.this)) {
            if (mSharePreferenceHelper.getValue("catStep").equalsIgnoreCase("0"))
                getSubCategoryRequest(mSharePreferenceHelper.getValue("root_category_id"));
            else
                getSubCategoryRequest(mSharePreferenceHelper.getValue("sub_category_id"));
        }
    }
}
