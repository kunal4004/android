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
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedOutFragment extends Fragment implements View.OnClickListener {
	public WButton login;
	public WButton register;
	public RelativeLayout valuedMember;
	public RelativeLayout loyalMember;
	public RelativeLayout vipMember;
	public WTextView applyForWRewards;
	public WTextView wRewardsTagLine;
	private BottomNavigator mBottomNavigator;
	private ScrollView scrollLoggedOutLoggedIn;
	private String TAG = this.getClass().getSimpleName();

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wrewards_loggedout_loggedin_notlinked, container, false);
		try {
			mBottomNavigator = (BottomNavigator) getActivity();
			mBottomNavigator.removeToolbar();
		} catch (IllegalStateException ex) {
			Log.d(TAG, ex.toString());
		}
		view.findViewById(R.id.wRewars_linkaccounts).setVisibility(View.GONE);
		login = view.findViewById(R.id.wRewars_login);
		register = view.findViewById(R.id.wRewars_register);
		valuedMember = view.findViewById(R.id.layoutValuedMember);
		loyalMember = view.findViewById(R.id.layoutLoyalMember);
		vipMember = view.findViewById(R.id.layoutVipMember);
		applyForWRewards = view.findViewById(R.id.applyForWRewards);
		wRewardsTagLine = view.findViewById(R.id.wRewards_tag_line);
		scrollLoggedOutLoggedIn = view.findViewById(R.id.scrollLoggedOutLoggedIn);
		wRewardsTagLine.setText(getResources().getText(R.string.wrewards_logout_tag_line));
		login.setOnClickListener(this);
		register.setOnClickListener(this);
		valuedMember.setOnClickListener(this);
		loyalMember.setOnClickListener(this);
		vipMember.setOnClickListener(this);
		applyForWRewards.setOnClickListener(this);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SIGNED_OUT);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.wRewars_login:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSSIGNIN);
				ScreenManager.presentSSOSignin(getActivity());
				break;
			case R.id.wRewars_register:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSREGISTER);
				ScreenManager.presentSSORegister(getActivity());
				break;
			case R.id.layoutValuedMember:
				redirectToWRewardsMemberActivity(0);
				break;
			case R.id.layoutLoyalMember:
				redirectToWRewardsMemberActivity(1);
				break;
			case R.id.layoutVipMember:
				redirectToWRewardsMemberActivity(2);
				break;
			case R.id.applyForWRewards:
				Utils.openExternalLink(getActivity(), WoolworthsApplication.getWrewardsLink());
				break;
			default:
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void redirectToWRewardsMemberActivity(int type) {
		startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class).putExtra("type", type));
		getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (mBottomNavigator != null)
			mBottomNavigator.removeToolbar();
	}

	public void scrollToTop() {
		ObjectAnimator anim = ObjectAnimator.ofInt(scrollLoggedOutLoggedIn, "scrollY", scrollLoggedOutLoggedIn.getScrollY(), 0);
		anim.setDuration(500).start();
	}
}
