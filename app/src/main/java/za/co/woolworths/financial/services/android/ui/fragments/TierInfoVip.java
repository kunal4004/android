package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.Tier;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.StatusInfoActivity;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class TierInfoVip extends Fragment implements StatusInfoActivity.InfoFragment {

    private VoucherResponse mVouchers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tier_info_vip, container, false);
        for (Tier t : mVouchers.tiers){
            if (t.rank == 3){
                ((TextView)view.findViewById(R.id.tier_info_vip_spend)).setText(getString(R.string.vip_spend, WFormatter.formatAmount(t.lowerBoundValue)));
            }
        }
        return view;
    }

    @Override
    public void setVouchers(VoucherResponse vouchers) {
        mVouchers = vouchers;
    }
}
