package za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.WrewardsLoggedinAndLinkedFragmentBinding;

import za.co.woolworths.financial.services.android.models.WRewardsCardDetails;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.message.CLIGetMessageResponse;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsErrorFragment;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsOverviewFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsSavingsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.base.WRewardsFragment;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 05/01/2017.
 */
public class WRewardsLoggedinAndLinkedFragment extends BaseFragment<WrewardsLoggedinAndLinkedFragmentBinding, WRewardLoggedInViewModel> {

	private TabLayout tabLayout;
	private ViewPager viewPager;
	private WRewardsFragmentPagerAdapter adapter;
	private ProgressBar progressBar;
	private LinearLayout fragmentView;
	private ErrorHandlerView mErrorHandlerView;
	private RelativeLayout mRlConnect;
	private WGlobalState mWGlobalState;
	public static final int DEFAULT_VOUCHER_COUNT = 0;
	public boolean isWrewardsCalled;
	public boolean isCardDetailsCalled;
	public CardDetailsResponse cardDetailsResponse;
	public VoucherResponse voucherResponse;
	private WRewardLoggedInViewModel wRewardViewModel;
	private GetVoucher getVoucherAsync;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wRewardViewModel = ViewModelProviders.of(this).get(WRewardLoggedInViewModel.class);
	}

	@Override
	public WRewardLoggedInViewModel getViewModel() {
		return wRewardViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.wrewards_loggedin_and_linked_fragment;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		hideToolbar();
		setTitle(getString(R.string.nav_item_wrewards));
		mWGlobalState = ((WoolworthsApplication) getActivity().getApplication()).getWGlobalState();
		viewPager = getViewDataBinding().viewpager;
		progressBar = getViewDataBinding().progressBar;
		fragmentView = getViewDataBinding().fragmentView;
		tabLayout = getViewDataBinding().tabs;
		viewPager.setOffscreenPageLimit(3);
		progressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		mRlConnect = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), mRlConnect);
		mErrorHandlerView.setMargin(mRlConnect, 0, 0, 0, 0);
		loadReward();
		loadCardDetails();
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(getActivity())) {
					mErrorHandlerView.hideErrorHandler();
					loadReward();
				}
			}
		});

		getMessageResponse().execute();
	}

	private void setupViewPager(ViewPager viewPager, VoucherResponse voucherResponse, CardDetailsResponse cardResponse) {
		Bundle bundle = new Bundle();
		bundle.putString("WREWARDS", Utils.objectToJson(voucherResponse));
		if (cardResponse != null)
			bundle.putString("CARD_DETAILS", Utils.objectToJson(cardResponse));
		adapter = new WRewardsFragmentPagerAdapter(getChildFragmentManager(), bundle);
		adapter.addFrag(new WRewardsOverviewFragment(), getString(R.string.overview));
		adapter.addFrag(new WRewardsVouchersFragment(), getString(R.string.vouchers));
		adapter.addFrag(new WRewardsSavingsFragment(), getString(R.string.savings));
		viewPager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		tabLayout.setupWithViewPager(viewPager);
		viewPager.invalidate();
		if (voucherResponse.voucherCollection.vouchers != null)
			setupTabIcons(voucherResponse.voucherCollection.vouchers.size());
		else
			setupTabIcons(DEFAULT_VOUCHER_COUNT);
	}

	private void setupTabIcons(int activeVoucherCount) {
		String[] tabTitle = {getActivity().getString(R.string.overview), getActivity().getString(R.string.vouchers), getActivity().getString(R.string.savings)};

		for (int i = 0; i < tabTitle.length; i++) {
			tabLayout.getTabAt(i).setCustomView(prepareTabView(i, tabTitle, activeVoucherCount));
		}
		tabLayout.getTabAt(0).getCustomView().setSelected(true);
	}

	private View prepareTabView(int pos, String[] tabTitle, int activeVoucherCount) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.wrewards_custom_tab, null);
		TextView tv_title = view.findViewById(R.id.tv_title);
		TextView tv_count = view.findViewById(R.id.tv_count);
		tv_title.setText(tabTitle[pos]);
		if (pos == 1 && activeVoucherCount > 0) {
			tv_count.setVisibility(View.VISIBLE);
			tv_count.setText("" + activeVoucherCount);
		} else {
			tv_count.setVisibility(View.GONE);
		}

		return view;
	}

	private void loadReward() {
		getVoucherAsync = getWRewards();
		getVoucherAsync.execute();
	}

	private GetVoucher getWRewards() {
		return new GetVoucher(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				handleVoucherResponse(((VoucherResponse) object));
			}

			@Override
			public void onFailure(String errorMessage) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
			}
		});
	}

	public void handleVoucherResponse(VoucherResponse response) {
		try {
			switch (response.httpCode) {
				case 200:
					mWGlobalState.setRewardSignInState(true);
					mWGlobalState.setRewardHasExpired(false);
					if (response.voucherCollection.vouchers != null) {
						getBottomNavigator().addBadge(3, response.voucherCollection.vouchers.size());
					} else {
						clearVoucherCounter();
					}
					voucherResponse = response;
					isWrewardsCalled = true;
					handleWrewardsAndCardDetailsResponse();
					break;
				case 440:
					progressBar.setVisibility(View.GONE);
					fragmentView.setVisibility(View.VISIBLE);
					clearVoucherCounter();
					mWGlobalState.setRewardHasExpired(true);
					mWGlobalState.setRewardSignInState(false);
					SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), response.response.stsParams);
					Utils.setBadgeCounter(getActivity(), 0);
					Intent intent = new Intent();
					WRewardsFragment mParentFragment = (WRewardsFragment) getParentFragment();
					if (mParentFragment != null) {
						mParentFragment.onActivityResult(WRewardsFragment.FRAGMENT_CODE_2, Activity.RESULT_OK, intent);
					}
					getFragmentManager().popBackStack();
					SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
					break;
				default:
					progressBar.setVisibility(View.GONE);
					fragmentView.setVisibility(View.VISIBLE);
					clearVoucherCounter();
					mWGlobalState.setRewardSignInState(false);
					setupErrorViewPager(viewPager);
					break;
			}
		} catch (Exception ignored) {
		}
	}

	private void setupErrorViewPager(ViewPager viewPager) {
		Bundle bundle = new Bundle();
		bundle.putString("WREWARDS", "");
		adapter = new WRewardsFragmentPagerAdapter(getChildFragmentManager(), bundle);
		adapter.addFrag(new WRewardsErrorFragment(), getString(R.string.overview));
		adapter.addFrag(new WRewardsErrorFragment(), getString(R.string.vouchers));
		adapter.addFrag(new WRewardsErrorFragment(), getString(R.string.savings));
		viewPager.setAdapter(adapter);
		tabLayout.setupWithViewPager(viewPager);

		try {
			setupTabIcons(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void clearVoucherCounter() {
		getBottomNavigator().addBadge(3, 0);
	}

	public void loadCardDetails() {
		new WRewardsCardDetails(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				isCardDetailsCalled = true;
				cardDetailsResponse = (CardDetailsResponse) object;
				handleWrewardsAndCardDetailsResponse();
			}

			@Override
			public void onFailure(String e) {
				isCardDetailsCalled = true;
				handleWrewardsAndCardDetailsResponse();
			}
		}).execute();
	}

	public void handleWrewardsAndCardDetailsResponse() {
		if (isCardDetailsCalled && isWrewardsCalled) {
			progressBar.setVisibility(View.GONE);
			fragmentView.setVisibility(View.VISIBLE);
			setupViewPager(viewPager, voucherResponse, cardDetailsResponse);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private CLIGetMessageResponse getMessageResponse() {
		return new CLIGetMessageResponse(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				try {
					MessageResponse messageResponse = (MessageResponse) object;
					if (messageResponse.unreadCount > 0) {
						int unreadCount = messageResponse.unreadCount;
						if (TextUtils.isEmpty(String.valueOf(unreadCount)))
							unreadCount = 0;
						Utils.setBadgeCounter(getActivity(), unreadCount);
						addBadge(BottomNavigationActivity.INDEX_ACCOUNT, unreadCount);
					} else {
						Utils.removeBadgeCounter(getActivity());
						addBadge(BottomNavigationActivity.INDEX_ACCOUNT, 0);
					}
				} catch (Exception ignored) {
				}
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			setTitle(getString(R.string.nav_item_wrewards));
			//showToolbar();
		}
	}
}
