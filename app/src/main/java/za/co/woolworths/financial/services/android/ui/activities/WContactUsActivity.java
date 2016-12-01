package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.Contact;
import za.co.woolworths.financial.services.android.ui.adapters.ContactUsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsCustomerServiceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsFinancialServiceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsMySchoolFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsOnlineFragment;
import za.co.woolworths.financial.services.android.ui.fragments.ContactUsWRewardsFragment;

import static android.R.attr.id;
import static za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity.toolbar;

public class WContactUsActivity extends AppCompatActivity {

    public Toolbar toolbar;
    public TabLayout tabLayout;
    public ViewPager viewPager;
    ContactUsFragmentPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.parseColor("#30000000"),Color.parseColor("#000000"));
        changeTabsFont(tabLayout);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter=new ContactUsFragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ContactUsFinancialServiceFragment(), "Financial Services");
        adapter.addFrag(new ContactUsOnlineFragment(), "online");
        adapter.addFrag(new ContactUsCustomerServiceFragment(), "customer service");
        adapter.addFrag(new ContactUsWRewardsFragment(), "WRewards");
        adapter.addFrag(new ContactUsMySchoolFragment(), "MySchool");
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);

    }
}
