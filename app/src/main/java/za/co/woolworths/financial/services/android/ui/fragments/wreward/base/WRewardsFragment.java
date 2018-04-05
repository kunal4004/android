package za.co.woolworths.financial.services.android.ui.fragments.wreward.base;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.WrewardsFragmentBinding;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedOutFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsLoggedinAndNotLinkedFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in.WRewardsLoggedinAndLinkedFragment;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;

public class WRewardsFragment extends BaseFragment<WrewardsFragmentBinding, WRewardViewModel> implements WRewardNavigator {

	private WRewardViewModel mWRewardViewModel;
	private final String TAG = this.getClass().getSimpleName();
	private boolean mRewardSignInState;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWRewardViewModel = ViewModelProviders.of(this).get(WRewardViewModel.class);
		mWRewardViewModel.setNavigator(this);

		getViewModel().consumeObservable(new Consumer<Object>() {
			@Override
			public void accept(Object object) throws Exception {
				Activity activity = getActivity();
				if (activity != null) {

					if (!SessionUtilities.getInstance().isUserAuthenticated()){
						addBadge(BottomNavigationActivity.INDEX_REWARD, 0);
						initialize();
						SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
					}
				}
			}
		});
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
		Activity activity = getActivity();

		removeAllChildFragments((AppCompatActivity) getActivity());

		boolean isUserAuthenticated = SessionUtilities.getInstance().isUserAuthenticated();
		boolean isC2User = SessionUtilities.getInstance().isC2User();

		if (isUserAuthenticated && isC2User) {
			//user is linked and signed in
			linkSignIn();
		} else if (isUserAuthenticated && !isC2User) {
			//user is not linked
			//but signed in
			notLinkSignIn();
		} else if (!isUserAuthenticated) {
			//user is signed out
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

	public void linkSignIn() {
		hideToolbar();
		FragmentManager childFragMan = getChildFragmentManager();
		FragmentTransaction childFragTrans = childFragMan.beginTransaction();
		WRewardsLoggedinAndLinkedFragment fragmentChild = new WRewardsLoggedinAndLinkedFragment();
		childFragTrans.add(R.id.content_frame, fragmentChild);
		childFragTrans.commitAllowingStateLoss();
	}

	private void notLinkSignIn() {
		hideToolbar();
		FragmentManager childFragMan = getChildFragmentManager();
		FragmentTransaction childFragTrans = childFragMan.beginTransaction();
		WRewardsLoggedinAndNotLinkedFragment fragmentChild = new WRewardsLoggedinAndNotLinkedFragment();
		childFragTrans.add(R.id.content_frame, fragmentChild);
		childFragTrans.commitAllowingStateLoss();
	}

	private void signOut() {
		hideToolbar();
		FragmentManager childFragMan = getChildFragmentManager();
		FragmentTransaction childFragTrans = childFragMan.beginTransaction();
		WRewardsLoggedOutFragment fragmentChild = new WRewardsLoggedOutFragment();
		childFragTrans.add(R.id.content_frame, fragmentChild);
		childFragTrans.commitAllowingStateLoss();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
			SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
		}
		initialize();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			hideToolbar();
			initialize();
		}
		setTitle(getString(R.string.wrewards));
	}
}
