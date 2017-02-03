package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

import za.co.woolworths.financial.services.android.models.dto.Contact;
import za.co.woolworths.financial.services.android.ui.adapters.ContactUsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsCustomerServiceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsFinancialServiceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsMySchoolFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsOnlineFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsWRewardsFragment;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.R.attr.id;
import static za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.toolbar;

public class WContactUsActivity extends BaseActivity {

    public Toolbar toolbar;
    public TabLayout tabLayout;
    public ViewPager viewPager;
    ContactUsFragmentPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us_activity);
        Utils.updateStatusBarBackground(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont(tabLayout);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter=new ContactUsFragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ContactUsFinancialServiceFragment(), getString(R.string.contact_us_financial_services));
        adapter.addFrag(new ContactUsOnlineFragment(), getString(R.string.contact_us_online));
        adapter.addFrag(new ContactUsCustomerServiceFragment(), getString(R.string.contact_us_customer_service));
        adapter.addFrag(new ContactUsWRewardsFragment(), getString(R.string.contact_us_wrewards));
        adapter.addFrag(new ContactUsMySchoolFragment(), getString(R.string.contact_us_myschool));
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
    }
}
