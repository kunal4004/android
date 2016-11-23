package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.CardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;

public class MyAccountCardsActivity extends AppCompatActivity {

    WCustomViewPager pager,fragmentPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_cards);
        pager = (WCustomViewPager) findViewById(R.id.myAccountsCardPager);
        fragmentPager=(WCustomViewPager)findViewById(R.id.fragmentpager);
        pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivity.this));
        pager.setPageMargin(16);
        fragmentPager.setAdapter(new CardsFragmentPagerAdapter(getSupportFragmentManager()));


    }
}
