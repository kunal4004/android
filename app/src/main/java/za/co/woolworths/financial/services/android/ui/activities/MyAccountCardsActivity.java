package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;


import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.CardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;
import za.co.woolworths.financial.services.android.ui.views.WFragmentViewPager;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class MyAccountCardsActivity extends AppCompatActivity {

    ViewPager pager;
    WFragmentViewPager fragmentPager;
    public Toolbar toolbar;
    public WTextView toolbarTextView;
    public LinearLayout cardsLayoutBackground;
    boolean isCreditCard = false;
    boolean isStoreCard = true;
    boolean isPersonalCard = true;
    CardsFragmentPagerAdapter fragmentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_cards);
        Utils.updateStatusBarBackground(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        pager = (ViewPager) findViewById(R.id.myAccountsCardPager);
        fragmentPager=(WFragmentViewPager) findViewById(R.id.fragmentpager);
        toolbarTextView=(WTextView)findViewById(R.id.toolbarText);
        cardsLayoutBackground=(LinearLayout)findViewById(R.id.cardsLayoutBackground);
        pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivity.this));
        pager.setPageMargin(16);
        //fragmentPager.setAdapter(new CardsFragmentPagerAdapter(getSupportFragmentManager()));
        fragmentPager.setPagingEnabled(false);
        setUpFragmentPager(fragmentPager);

       /* pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fragmentPager.setPagingEnabled(true);
                fragmentPager.onTouchEvent(event);
                fragmentPager.setPagingEnabled(false);
                return false;
            }
        });*/

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


    }

    public void changeViewPagerAndActionBarBackground(int position)
    {
                     switch (position){
                         case 0:
                             toolbarTextView.setText("STORE CARD");
                             cardsLayoutBackground.setBackgroundResource(R.drawable.accounts_storecard_background);
                             break;
                         case 1:
                             toolbarTextView.setText("CREDIT CARD");
                             cardsLayoutBackground.setBackgroundResource(R.drawable.accounts_blackcreditcard_background);
                             break;
                         case 2:
                             toolbarTextView.setText("PERSONAL LOAN");
                             cardsLayoutBackground.setBackgroundResource(R.drawable.accounts_personalloancard_background);
                             break;
                     }
    }

    public void setUpFragmentPager(ViewPager viewPager)
    {
        fragmentsAdapter =new CardsFragmentPagerAdapter(getSupportFragmentManager());
        fragmentsAdapter.addFrag(isStoreCard ? new WStoreCardFragment() : new WStoreCardEmptyFragment());
        fragmentsAdapter.addFrag(isCreditCard ? new WCreditCardFragment() : new WStoreCardEmptyFragment());
        fragmentsAdapter.addFrag(isStoreCard ? new WStoreCardFragment() : new WStoreCardEmptyFragment());
        viewPager.setAdapter(fragmentsAdapter);

    }
}
