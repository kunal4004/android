package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.ContactUsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.WRewardsLoyalMembersInfoFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WRewardsValuedMembersInfoFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WRewardsVipMembersInfoFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

import static za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity.appbar;
import static za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity.mToolbar;

public class WRewardsMembersInfoActivity extends AppCompatActivity {

    public Toolbar toolbar;
    public TabLayout tabLayout;
    public ViewPager viewPager;
    ContactUsFragmentPagerAdapter adapter;
    CollapsingToolbarLayout collapsingToolbarLayout;
    public AppBarLayout appBar;
    public WTextView toolbarTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrewards_members_info_activity);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.green));
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        NestedScrollView scrollView = (NestedScrollView) findViewById (R.id.nest_scrollview);
        scrollView.setFillViewport (true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        appBar=(AppBarLayout)findViewById(R.id.appBarLayout);
        toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont(tabLayout);

        appBar.addOnOffsetChangedListener(new   AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -collapsingToolbarLayout.getHeight() + toolbar.getHeight()) {
                    toolbarTextView.setText("WRewards");
                }
                else {
                    toolbarTextView.setText("");
                }
            }
        });
    }
    private void setupViewPager(ViewPager viewPager) {
        adapter=new ContactUsFragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new WRewardsValuedMembersInfoFragment(), getString(R.string.valued));
        adapter.addFrag(new WRewardsLoyalMembersInfoFragment(), getString(R.string.loyal));
        adapter.addFrag(new WRewardsVipMembersInfoFragment(), getString(R.string.vip));
        viewPager.setAdapter(adapter);
    }


    private void changeTabsFont(TabLayout tabLayout) {
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/WFutura-Medium.ttf");
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(type);
                }
            }
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

    }
}
