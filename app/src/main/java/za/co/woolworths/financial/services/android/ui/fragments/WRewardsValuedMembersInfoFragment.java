package za.co.woolworths.financial.services.android.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsValuedMembersInfoFragment extends Fragment {

    public WTextView statustext3;
    public WTextView statustext4;
    public WTextView statustext5;
    public WTextView statustext6;
    public WTextView infoHeaderText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.wrewards_valued_loyal_vip_info_fragment, container, false);
        view.findViewById(R.id.statusImage3).setBackgroundResource(R.drawable.wrewardsbenefitsinactive);
        view.findViewById(R.id.statusImage4).setBackgroundResource(R.drawable.wrewardsbenefitsinactive);
        view.findViewById(R.id.statusImage5).setBackgroundResource(R.drawable.wrewardsbenefitsinactive);
        view.findViewById(R.id.statusImage6).setBackgroundResource(R.drawable.wrewardsbenefitsinactive);
        statustext3=(WTextView)view.findViewById(R.id.statusText3);
        statustext4=(WTextView)view.findViewById(R.id.statusText4);
        statustext5=(WTextView)view.findViewById(R.id.statusText5);
        statustext6=(WTextView)view.findViewById(R.id.statusText6);
        infoHeaderText=(WTextView)view.findViewById(R.id.infoHeaderText);
        statustext3.setTextColor(Color.parseColor("#4d000000"));
        statustext4.setTextColor(Color.parseColor("#4d000000"));
        statustext5.setTextColor(Color.parseColor("#4d000000"));
        statustext6.setTextColor(Color.parseColor("#4d000000"));
        infoHeaderText.setText(getActivity().getString(R.string.info_valued_header));
        return view;

    }
}