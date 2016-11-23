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

public class TierInfoLoyal extends Fragment implements StatusInfoActivity.InfoFragment {

    private VoucherResponse mVouchers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tier_info_loyal, container, false);
        for (Tier t : mVouchers.tiers){
            if (t.rank == 2){
                ((TextView)view.findViewById(R.id.tier_info_loyal_spend)).setText(getString(R.string.loyal_spend, WFormatter.formatAmount(t.lowerBoundValue),WFormatter.formatAmount(t.upperBoundValue)));
            }
        }
        return view;
    }

    @Override
    public void setVouchers(VoucherResponse vouchers) {
        mVouchers = vouchers;
    }
}
