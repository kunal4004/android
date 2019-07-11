package za.co.woolworths.financial.services.android.ui.fragments.wreward;


import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WRewardsLoyalMembersInfoFragment extends Fragment{
    public WTextView statustext6;
    public WTextView infoHeaderText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.wrewards_valued_loyal_vip_info_fragment, container, false);
        view.findViewById(R.id.statusImage6).setBackgroundResource(R.drawable.wrewardsbenefitsinactive);
        statustext6=(WTextView)view.findViewById(R.id.statusText6);
        infoHeaderText=(WTextView)view.findViewById(R.id.infoHeaderText);
        statustext6.setTextColor(Color.parseColor("#4d000000"));
        infoHeaderText.setText(getActivity().getString(R.string.info_loyal_header));
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_TIER_INFO);
    }
}