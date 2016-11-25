package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;


import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.CardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.views.WFragmentViewPager;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;

import static android.R.attr.width;

public class MyAccountCardsActivity extends AppCompatActivity {

    ViewPager pager;
    WFragmentViewPager fragmentPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_cards);
        pager = (ViewPager) findViewById(R.id.myAccountsCardPager);
        fragmentPager=(WFragmentViewPager) findViewById(R.id.fragmentpager);
        pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivity.this));
        pager.setPageMargin(16);
        fragmentPager.setAdapter(new CardsFragmentPagerAdapter(getSupportFragmentManager()));

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fragmentPager.setPagingEnabled(true);
                fragmentPager.onTouchEvent(event);
                fragmentPager.setPagingEnabled(false);
                return false;
            }
        });


    }
}
