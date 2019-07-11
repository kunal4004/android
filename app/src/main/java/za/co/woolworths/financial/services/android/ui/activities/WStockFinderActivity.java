package za.co.woolworths.financial.services.android.ui.activities;

import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import za.co.woolworths.financial.services.android.ui.adapters.StockFinderFragmentAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.StoreFinderListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.StoreFinderMapFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.GoogleMapViewPager;
import za.co.woolworths.financial.services.android.util.UpdateStoreFinderFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class WStockFinderActivity extends AppCompatActivity implements StoreFinderMapFragment.SlidePanelEvent, View.OnClickListener {

    private AppBarLayout mAppBarLayout;
    private int mActionBarSize;
    public GoogleMapViewPager mViewPager;
    public StockFinderFragmentAdapter mPagerAdapter;
    private String mProductName;
    private String mContactInto = null;
    public interface RecyclerItemSelected {
        void onRecyclerItemClick(View v, int position, String filterType);

        void onQuantitySelected(int quantity);
    }

    private final float LIGHTER_TEXT = 0.3f, NORMAL_TEXT = 1.0f;
    private WTextView tvMapView, tvListView;
    private ImageView imListView, imMapView;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(WStockFinderActivity.this);
        setContentView(R.layout.stock_finder_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back24);

        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            mProductName = mBundle.getString("PRODUCT_NAME");
            mContactInto = mBundle.getString("CONTACT_INFO");
        }

        mViewPager = findViewById(R.id.viewpager);
        final WTextView toolbarTextView = findViewById(R.id.toolbarText);
        mAppBarLayout = findViewById(R.id.appBarLayout);
        WButton btnOnLocationService = findViewById(R.id.buttonLocationOn);
        WButton buttonBackToProducts = findViewById(R.id.buttonBackToProducts);
        buttonBackToProducts.setOnClickListener(this);
        btnOnLocationService.setOnClickListener(this);
        WButton btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(this);
        toolbarTextView.setText(mProductName);

        ViewTreeObserver viewTreeObserver = toolbarTextView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = toolbarTextView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                if (toolbarTextView.getLineCount() > 2) {
                    int endOfLastLine = toolbarTextView.getLayout().getLineEnd(2);
                    String newVal = toolbarTextView.getText().subSequence(0, endOfLastLine - 3) + "...";
                    toolbarTextView.setText(newVal);
                }
            }
        });

        setupViewPager(mViewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setupTabIcons();

        TypedArray attrs = WStockFinderActivity.this.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        mActionBarSize = (int) attrs.getDimension(0, 0) * 2;

        mViewPager.addOnPageChangeListener(pageChangeListener);
    }

    private void setupViewPager(GoogleMapViewPager viewPager) {
        mPagerAdapter = new StockFinderFragmentAdapter(getSupportFragmentManager());
        mPagerAdapter.addFrag(StoreFinderMapFragment.newInstance(mContactInto), getString(R.string.stock_finder_map_view));
        mPagerAdapter.addFrag(StoreFinderListFragment.newInstance(mContactInto), getString(R.string.stock_finder_list_view));
        viewPager.setAdapter(mPagerAdapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setCustomView(R.layout.stockfinder_custom_tab);
        tabLayout.getTabAt(1).setCustomView(R.layout.stockfinder_custom_tab);

        View mapView = tabLayout.getTabAt(0).getCustomView();
        View listView = tabLayout.getTabAt(1).getCustomView();

        imMapView = mapView.findViewById(R.id.tabIcon);
        tvMapView = mapView.findViewById(R.id.textIcon);
        imListView = listView.findViewById(R.id.tabIcon);
        tvListView = listView.findViewById(R.id.textIcon);

        tvMapView.setText(getString(R.string.stock_finder_map_view));
        tvListView.setText(getString(R.string.stock_finder_list_view));

        imMapView.setImageResource(R.drawable.mapview);
        imListView.setImageResource(R.drawable.listview);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        enableMapTab();
                        break;
                    case 1:
                        enableListTab();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        enableMapTab();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        onNavigateBack();
    }

    private void enableMapTab() {
        tvMapView.setAlpha(NORMAL_TEXT);
        imMapView.setAlpha(NORMAL_TEXT);
        tvListView.setAlpha(LIGHTER_TEXT);
        imListView.setAlpha(LIGHTER_TEXT);
    }

    private void enableListTab() {
        tvMapView.setAlpha(LIGHTER_TEXT);
        imMapView.setAlpha(LIGHTER_TEXT);
        tvListView.setAlpha(NORMAL_TEXT);
        imListView.setAlpha(NORMAL_TEXT);
    }

    private void setLayoutParams(int paramsHeight) {
        LinearLayout.LayoutParams lsp = (LinearLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        lsp.height = paramsHeight;
        mAppBarLayout.setLayoutParams(lsp);
    }

    @Override
    public void slidePanelAnchored() {
        setLayoutParams(0);
    }

    @Override
    public void slidePanelCollapsed() {
        setLayoutParams(mActionBarSize);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonBackToProducts:
                finish();
                onNavigateBack();
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
                break;
            default:
                break;
        }
    }


    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int newPosition) {
            selectPage(newPosition);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };

    private void selectPage(int position) {
        UpdateStoreFinderFragment fragmentToShow = (UpdateStoreFinderFragment) mPagerAdapter.getItem(position);
        if (fragmentToShow != null) {
            fragmentToShow.onFragmentUpdate();
        }
    }

    private void onNavigateBack() {
        if (TextUtils.isEmpty(mContactInto))
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
        else
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

}