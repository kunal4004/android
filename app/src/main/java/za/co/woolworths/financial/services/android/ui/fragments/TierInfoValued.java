package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.StatusInfoActivity;

public class TierInfoValued extends Fragment implements StatusInfoActivity.InfoFragment {

    private VoucherResponse mVouchers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tier_info_valued, container, false);
        return view;
    }

    @Override
    public void setVouchers(VoucherResponse vouchers) {
        mVouchers = vouchers;
    }
}
