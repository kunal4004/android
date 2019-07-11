package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import za.co.woolworths.financial.services.android.ui.adapters.ContactUsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoyalMembersInfoFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsValuedMembersInfoFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVipMembersInfoFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

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
        Utils.updateStatusBarBackground(this, R.color.green);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nest_scrollview);
        scrollView.setFillViewport(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        appBar = (AppBarLayout) findViewById(R.id.appBarLayout);
        toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont(tabLayout);
        if (getIntent().hasExtra("type"))
            viewPager.setCurrentItem(getIntent().getIntExtra("type", 0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarTextView.setLetterSpacing(0.2f);
        }
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -collapsingToolbarLayout.getHeight() + toolbar.getHeight()) {
                    toolbarTextView.setText("WRewards");
                } else {
                    toolbarTextView.setText("");
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ContactUsFragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new WRewardsValuedMembersInfoFragment(), getString(R.string.valued));
        adapter.addFrag(new WRewardsLoyalMembersInfoFragment(), getString(R.string.loyal));
        adapter.addFrag(new WRewardsVipMembersInfoFragment(), getString(R.string.vip));
        viewPager.setAdapter(adapter);
    }


    private void changeTabsFont(TabLayout tabLayout) {
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/WFutura-Medium.ttf");
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(type);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((TextView) tabViewChild).setLetterSpacing(0.1f);
                    }
                }
            }
        }
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
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);

    }
}
