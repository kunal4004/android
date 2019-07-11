package za.co.woolworths.financial.services.android.ui.fragments.wreward;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

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
	public ScrollView scrollLoggedOutLoggedIn;
	public String TAG = this.getClass().getSimpleName();

	private BottomNavigator mBottomNavigator;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.wrewards_loggedout_loggedin_notlinked, container, false);
		view.findViewById(R.id.layoutLoginLogout).setVisibility(View.GONE);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
			mBottomNavigator.removeToolbar();
		} catch (IllegalStateException ex) {
			Log.d(TAG, ex.toString());
		}
		scrollLoggedOutLoggedIn = view.findViewById(R.id.scrollLoggedOutLoggedIn);
		valuedMember = view.findViewById(R.id.layoutValuedMember);
		loyalMember = view.findViewById(R.id.layoutLoyalMember);
		vipMember = view.findViewById(R.id.layoutVipMember);
		wRewars_linkaccounts = view.findViewById(R.id.wRewars_linkaccounts);
		applyForWRewards = view.findViewById(R.id.applyForWRewards);
		wRewardsTagLine = view.findViewById(R.id.wRewards_tag_line);
		valuedMember.setOnClickListener(this);
		loyalMember.setOnClickListener(this);
		vipMember.setOnClickListener(this);
		wRewars_linkaccounts.setOnClickListener(this);
		applyForWRewards.setOnClickListener(this);
		wRewardsTagLine.setText(getResources().getText(R.string.wrewards_unlinked_tag_line));
		wRewardsTagLine.setTextColor(getResources().getColor(R.color.black_50));
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SIGNED_IN_NOT_LINKED);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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
				Utils.openExternalLink(getActivity(), WoolworthsApplication.getWrewardsLink());
				break;
			default:
				break;
		}
	}

	public void redirectToWRewardsMemberActivity(int type) {
		startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class).putExtra("type", type));
		getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (mBottomNavigator != null)
				mBottomNavigator.removeToolbar();
		}
	}

	public void scrollToTop() {
		ObjectAnimator anim = ObjectAnimator.ofInt(scrollLoggedOutLoggedIn, "scrollY", scrollLoggedOutLoggedIn.getScrollY(), 0);
		anim.setDuration(500).start();
	}
}
