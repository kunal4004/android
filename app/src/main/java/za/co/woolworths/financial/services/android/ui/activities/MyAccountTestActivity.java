package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.TestViewpagerAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.MyAccountsFragment;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;

public class MyAccountTestActivity extends AppCompatActivity {

    WCustomViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_test);
        Utils.updateStatusBarBackground(this);
        pager=(WCustomViewPager)findViewById(R.id.pager);
        pager.setAdapter(new TestViewpagerAdapter(this));


    }
}
