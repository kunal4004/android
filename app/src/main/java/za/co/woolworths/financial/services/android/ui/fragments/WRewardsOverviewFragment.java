package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 05/01/2017.
 */


public class WRewardsOverviewFragment extends Fragment {
    public ImageView infoImage;
    public WTextView tireStatus;
    public WTextView savings;
    public WTextView toNextTire;
    public RelativeLayout toNextTireLayout;
    public VoucherResponse voucherResponse;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_overview_fragment, container, false);
        Bundle bundle=getArguments();
        voucherResponse=new Gson().fromJson(bundle.getString("WREWARDS"),VoucherResponse.class);
        String currentStatus = voucherResponse.tierInfo.currentTier.toUpperCase();
        infoImage=(ImageView)view.findViewById(R.id.infoImage);
        tireStatus=(WTextView)view.findViewById(R.id.tireStatus);
        savings=(WTextView)view.findViewById(R.id.savings);
        toNextTire=(WTextView)view.findViewById(R.id.toNextTire);
        toNextTireLayout=(RelativeLayout) view.findViewById(R.id.toNextTireLayout);

        tireStatus.setText(voucherResponse.tierInfo.currentTier);
        savings.setText(WFormatter.formatAmount(voucherResponse.tierInfo.earned));
        if (currentStatus.equals(getString(R.string.valued)) || currentStatus.equals(getString(R.string.loyal))) {
            toNextTireLayout.setVisibility(View.VISIBLE);
            toNextTire.setText(WFormatter.formatAmount(voucherResponse.tierInfo.toSpend));
        }


        infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class));
            }
        });
        return view;
    }
}