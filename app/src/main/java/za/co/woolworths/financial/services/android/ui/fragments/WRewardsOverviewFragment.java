package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;

/**
 * Created by W7099877 on 05/01/2017.
 */


public class WRewardsOverviewFragment extends Fragment {
    public ImageView infoImage;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_overview_fragment, container, false);
        infoImage=(ImageView)view.findViewById(R.id.infoImage);
        infoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class));
            }
        });
        return view;
    }
}