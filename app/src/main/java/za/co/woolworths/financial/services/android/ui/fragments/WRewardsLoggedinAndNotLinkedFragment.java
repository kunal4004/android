package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;

import static com.awfs.coordination.R.id.applyForWRewards;
import static com.awfs.coordination.R.string.register;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedinAndNotLinkedFragment extends Fragment implements View.OnClickListener {
    public RelativeLayout valuedMember;
    public RelativeLayout loyalMember;
    public RelativeLayout vipMember;
    public WTextView wRewars_linkaccounts;
    public WTextView applyForWRewards;
    public WTextView wRewardsTagLine;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.wrewards_loggedout_loggedin_notlinked, container, false);
        view.findViewById(R.id.layoutLoginLogout).setVisibility(View.GONE);
        valuedMember=(RelativeLayout)view.findViewById(R.id.layoutValuedMember);
        loyalMember=(RelativeLayout)view.findViewById(R.id.layoutLoyalMember);
        vipMember=(RelativeLayout)view.findViewById(R.id.layoutVipMember);
        wRewars_linkaccounts=(WTextView) view.findViewById(R.id.wRewars_linkaccounts);
        applyForWRewards=(WTextView) view.findViewById(R.id.applyForWRewards);
        wRewardsTagLine=(WTextView) view.findViewById(R.id.wRewards_tag_line);
        valuedMember.setOnClickListener(this);
        loyalMember.setOnClickListener(this);
        vipMember.setOnClickListener(this);
        wRewars_linkaccounts.setOnClickListener(this);
        applyForWRewards.setOnClickListener(this);
        wRewardsTagLine.setText(getResources().getText(R.string.wrewards_tag_line_notlinked));
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.layoutValuedMember:
                redirectToWRewardsMemberActivity(0);
                break;
            case R.id.layoutLoyalMember:
                redirectToWRewardsMemberActivity(1);
                break;
            case R.id.layoutVipMember:
                redirectToWRewardsMemberActivity(2);
                break;
            case R.id.wRewars_linkaccounts:
                ScreenManager.presentSSOLinkAccounts(getActivity());
                break;
            case R.id.applyForWRewards:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WoolworthsApplication.getWrewardsLink())));
                break;
            default:
                break;
        }
    }
    public void redirectToWRewardsMemberActivity( int type)
    {
        startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class).putExtra("type",type));
    }

}
