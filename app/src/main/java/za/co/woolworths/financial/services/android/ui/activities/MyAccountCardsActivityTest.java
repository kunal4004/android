package za.co.woolworths.financial.services.android.ui.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.CardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;
import za.co.woolworths.financial.services.android.ui.views.WFragmentViewPager;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;

import static com.awfs.coordination.R.id.pager;

public class MyAccountCardsActivityTest extends AppCompatActivity {


    WCustomViewPager pager;
    WFragmentViewPager fragmentPager;
    public WTextView toolbarTextView;
    public LinearLayout cardsLayoutBackground;
    boolean isCreditCard = false;
    boolean isStoreCard = false;
    boolean isPersonalCard = false;
    CardsFragmentPagerAdapter fragmentsAdapter;
    private CollapsingToolbarLayout collapsingToolbarLayout = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_cards_test);
        Utils.updateStatusBarBackground(MyAccountCardsActivityTest.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
        pager = (WCustomViewPager) findViewById(R.id.myAccountsCardPager);
        fragmentPager = (WFragmentViewPager) findViewById(R.id.fragmentpager);
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nest_scrollview);
        scrollView.setFillViewport(true);
        pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivityTest.this));
        pager.setPageMargin(16);
        fragmentPager.setPagingEnabled(false);
        setUpFragmentPager(fragmentPager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                fragmentPager.setCurrentItem(position);
                changeViewPagerAndActionBarBackground(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        // dynamicToolbarColor();
        dynamicToolbarColor("#4f5051");

    }


    private void dynamicToolbarColor(String colorString) {
        collapsingToolbarLayout.setContentScrimColor(Color.parseColor(colorString));

    }

    public void changeViewPagerAndActionBarBackground(int position) {
        switch (position) {
            case 0:
                toolbarTextView.setText("STORE CARD");
                collapsingToolbarLayout.setBackgroundResource(R.drawable.accounts_storecard_background);
                dynamicToolbarColor("#4f5051");
                break;
            case 1:
                toolbarTextView.setText("CREDIT CARD");
                collapsingToolbarLayout.setBackgroundResource(R.drawable.accounts_blackcreditcard_background);
                dynamicToolbarColor("#2e353b");
                break;
            case 2:
                toolbarTextView.setText("PERSONAL LOAN");
                collapsingToolbarLayout.setBackgroundResource(R.drawable.accounts_personalloancard_background);
                dynamicToolbarColor("#312439");
                break;
        }
    }

    public void setUpFragmentPager(ViewPager viewPager) {
        fragmentsAdapter = new CardsFragmentPagerAdapter(getSupportFragmentManager());
        fragmentsAdapter.addFrag(isStoreCard ? new WStoreCardFragment() : new WStoreCardEmptyFragment());
        fragmentsAdapter.addFrag(isCreditCard ? new WCreditCardFragment() : new WCreditCardEmptyFragment());
        fragmentsAdapter.addFrag(isPersonalCard ? new WStoreCardFragment() : new WPersonalLoanEmptyFragment());
        viewPager.setAdapter(fragmentsAdapter);

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
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }
}
