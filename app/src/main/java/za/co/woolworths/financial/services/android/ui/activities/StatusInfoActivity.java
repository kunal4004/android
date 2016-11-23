package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.fragments.TierInfoLoyal;
import za.co.woolworths.financial.services.android.ui.fragments.TierInfoValued;
import za.co.woolworths.financial.services.android.ui.fragments.TierInfoVip;
import za.co.woolworths.financial.services.android.ui.views.FragmentSlider;

public class StatusInfoActivity extends FragmentActivity {
    public static final String ID = "ID";
    public static final String DATA = "DATA";

    private Fragment[] mFragments = {new TierInfoValued(), new TierInfoLoyal(), new TierInfoVip()};
    private VoucherResponse mVoucherResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_info_activity);
        mVoucherResponse = new Gson().fromJson(getIntent().getStringExtra(DATA), VoucherResponse.class);
        for (Fragment mFragment : mFragments) {
            ((InfoFragment) mFragment).setVouchers(mVoucherResponse);
        }
        FragmentSlider fragmentSlider = (FragmentSlider) findViewById(R.id.status_info_fragment_slider);
        fragmentSlider.setFragmentSliderAdapter(new FragmentSlider.FragmentSliderAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }
        });
        switch (getIntent().getIntExtra(ID, 0)){
            case R.id.tier_info_tier_1:
                fragmentSlider.setPage(0);
                break;
            case R.id.tier_info_tier_2:
                fragmentSlider.setPage(1);
                break;
            case R.id.tier_info_tier_3:
                fragmentSlider.setPage(2);
                break;
        }
    }

    public interface InfoFragment{
        void setVouchers(VoucherResponse vouchers);
    }
}
