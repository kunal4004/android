package za.co.woolworths.financial.services.android.ui.fragments.wreward.base;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.WrewardsFragmentBinding;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedOutFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in.WRewardsLoggedinAndLinkedFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedinAndNotLinkedFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class WRewardsFragment extends BaseFragment<WrewardsFragmentBinding, WRewardViewModel> implements WRewardNavigator {
	public static final int FRAGMENT_CODE_1 = 1;
	public static final int FRAGMENT_CODE_2 = 2;

	private WGlobalState mWGlobalState;
	private WRewardViewModel mWRewardViewModel;
	private String TAG = this.getClass().getSimpleName();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWGlobalState = ((WoolworthsApplication) getActivity().getApplication()).getWGlobalState();
		mWRewardViewModel = ViewModelProviders.of(this).get(WRewardViewModel.class);
		mWRewardViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initialize();
	}

	@Override
	public WRewardViewModel getViewModel() {
		return mWRewardViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.wrewards_fragment;
	}

	public void initialize() {
		removeAllChildFragments((AppCompatActivity) getActivity());
		boolean accountSignInState = mWGlobalState.getAccountSignInState();
		boolean rewardSignInState = mWGlobalState.getRewardSignInState();
		JWTDecodedModel jwtDecodedModel = Utils.getJWTDecoded(getActivity());
		if (accountSignInState && rewardSignInState) {
			//user is linked and signed in
			linkSignIn();
		} else if (accountSignInState && !rewardSignInState) {
			// sign in but reward state false
			//user is not linked
			//but signed in
			replaceFragment(jwtDecodedModel);
		} else if (!accountSignInState && rewardSignInState) {
			// authentication session expired
			// but reward state true
			replaceFragment(jwtDecodedModel);
		} else {
			// user is signed out
			signOut();
		}
	}

	public void removeAllChildFragments(AppCompatActivity activity) {
		if (activity != null) {
			FragmentManager fm = activity.getSupportFragmentManager(); // or 'getSupportFragmentManager();'
			int count = fm.getBackStackEntryCount();
			if (count > 0) {
				for (int i = 0; i < count; ++i) {
					fm.popBackStack();
				}
			}
		}
	}

	public void reloadFragment() {
		try {
			WRewardsFragment fragment = (WRewardsFragment)
					getFragmentManager().findFragmentById(R.id.frag_container);
			getFragmentManager().beginTransaction()
					.detach(fragment)
					.attach(fragment)
					.commit();
		} catch (ClassCastException ex) {
			Log.d(TAG, ex.toString());
		}
	}

	public void linkSignIn() {
		hideToolbar();
		FragmentManager childFragMan = getChildFragmentManager();
		FragmentTransaction childFragTrans = childFragMan.beginTransaction();
		WRewardsLoggedinAndLinkedFragment fragmentChild = new WRewardsLoggedinAndLinkedFragment();
		childFragTrans.add(R.id.content_frame, fragmentChild);
		childFragTrans.commit();
	}

	private void notLinkSignIn() {
		hideToolbar();
		FragmentManager childFragMan = getChildFragmentManager();
		FragmentTransaction childFragTrans = childFragMan.beginTransaction();
		WRewardsLoggedinAndNotLinkedFragment fragmentChild = new WRewardsLoggedinAndNotLinkedFragment();
		childFragTrans.add(R.id.content_frame, fragmentChild);
		childFragTrans.commit();
	}

	private void signOut() {
		hideToolbar();
		FragmentManager childFragMan = getChildFragmentManager();
		FragmentTransaction childFragTrans = childFragMan.beginTransaction();
		WRewardsLoggedOutFragment fragmentChild = new WRewardsLoggedOutFragment();
		childFragTrans.add(R.id.content_frame, fragmentChild);
		childFragTrans.commit();
		addBadge(BottomNavigationActivity.INDEX_ACCOUNT, 0);
		addBadge(BottomNavigationActivity.INDEX_REWARD, 0);
	}

	private void replaceFragment(JWTDecodedModel jwtDecodedModel) {
		if (jwtDecodedModel.AtgSession != null) {
			if (jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")) {
				//user is linked and signed in
				linkSignIn();
			} else {
				//user is not linked
				//but signed in
				notLinkSignIn();
			}
		} else {
			//user is signed out
			signOut();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		onSessionExpired();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FRAGMENT_CODE_1 && resultCode == Activity.RESULT_OK) {
			try {
				reloadFragment();
			} catch (Exception ex) {
			}
		} else if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
			(WoolworthsApplication.getInstance().getWGlobalState()).setAccountSignInState(true);
			removeAllChildFragments((AppCompatActivity) getActivity());
			reloadFragment();
		} else {
			try {
				signOut();
			} catch (Exception ignored) {
			}
		}
	}

	private void onSessionExpired() {
		try {
			if (!TextUtils.isEmpty(mWGlobalState.getNewSTSParams()) && mWGlobalState.rewardHasExpired()) {
			} else {
				if (mWGlobalState.rewardHasExpired()
						&& (mWGlobalState.getPressState().equalsIgnoreCase
						(WGlobalState.ON_CANCEL))) {
				} else if (mWGlobalState.getRewardSignInState()
						&& (mWGlobalState.getPressState().equalsIgnoreCase
						(WGlobalState.ON_SIGN_IN))) {
					mWGlobalState.setRewardHasExpired(false);
					mWGlobalState.setRewardSignInState(true);
					mWGlobalState.resetPressState();
					try {
						reloadFragment();
					} catch (Exception ex) {
					}
				} else {
				}
			}
			mWGlobalState.setRewardHasExpired(false);
			mWGlobalState.resetPressState();
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			hideToolbar();
			reloadFragment();
		} else {
			reloadFragment();
		}
		setTitle(getString(R.string.wrewards));
	}
}
