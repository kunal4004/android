package za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.WrewardsLoggedinAndLinkedFragmentBinding;
import com.crashlytics.android.Crashlytics;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;
import za.co.woolworths.financial.services.android.models.rest.reward.WRewardsCardDetails;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsErrorFragment;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsOverviewFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsSavingsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.WRewardsVouchersFragment;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.base.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;


/**
 * Created by W7099877 on 05/01/2017.
 */
public class WRewardsLoggedinAndLinkedFragment extends BaseFragment<WrewardsLoggedinAndLinkedFragmentBinding, WRewardLoggedInViewModel> implements WMaterialShowcaseView.IWalkthroughActionListener {

	private TabLayout tabLayout;
	private ViewPager viewPager;
	private WRewardsFragmentPagerAdapter adapter;
	private ProgressBar progressBar;
	private LinearLayout fragmentView;
	private ErrorHandlerView mErrorHandlerView;
	private RelativeLayout mRlConnect;
	public static final int DEFAULT_VOUCHER_COUNT = 0;
	public boolean isWrewardsCalled;
	public boolean isCardDetailsCalled;
	public CardDetailsResponse cardDetailsResponse;
	public VoucherResponse voucherResponse;
	private WRewardLoggedInViewModel wRewardViewModel;
	private Call<VoucherResponse> getVoucherAsync;
	private AsyncTask<String, String, CardDetailsResponse> wRewardsCardDetails;
	private String TAG = this.getClass().getSimpleName();
	boolean isActivityInForeground;

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

		viewPager = getViewDataBinding().viewpager;
		progressBar = getViewDataBinding().progressBar;
		fragmentView = getViewDataBinding().fragmentView;
		tabLayout = getViewDataBinding().tabs;
		viewPager.setOffscreenPageLimit(3);
		mRlConnect = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
		mErrorHandlerView = new ErrorHandlerView(getActivity(), mRlConnect);
		mErrorHandlerView.setMargin(mRlConnect, 0, 0, 0, 0);
		if (getCurrentStackIndex() == BottomNavigationActivity.INDEX_REWARD) {
			setTitle(getString(R.string.nav_item_wrewards));
			showToolbar();
			loadReward();
			loadCardDetails();
		}
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
					mErrorHandlerView.hideErrorHandler();
					loadReward();
				}
			}
		});
	}

	private void setupViewPager(ViewPager viewPager, VoucherResponse voucherResponse, CardDetailsResponse cardResponse) {
		if (!isAdded()) return;
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
			if (isActivityInForeground && getBottomNavigationActivity().getCurrentFragment() instanceof WRewardsFragment)
				showFeatureWalkthrough(tv_count);
		} else {
			tv_count.setVisibility(View.GONE);
		}
		return view;
	}

	private void loadReward() {
		getVoucherAsync = getWRewards();
	}

	private Call<VoucherResponse> getWRewards() {

		Call<VoucherResponse> voucherRequestCall =  OneAppService.INSTANCE.getVouchers();
		voucherRequestCall.enqueue(new CompletionHandler<>(new RequestListener<VoucherResponse>() {
			@Override
			public void onSuccess(VoucherResponse voucherResponse) {
				handleVoucherResponse((voucherResponse));
			}

			@Override
			public void onFailure(Throwable error) {
				if (error==null)return;
				mErrorHandlerView.networkFailureHandler(error.getMessage());
			}
		}));

		return voucherRequestCall;
	}

	public void handleVoucherResponse(VoucherResponse response) {
		int httpCode = response.httpCode;
		try {
			switch (httpCode) {
				case 200:
					if (response.voucherCollection.vouchers != null) {
						addBadge(BottomNavigationActivity.INDEX_REWARD, response.voucherCollection.vouchers.size());
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

					String stsParams = null;
					if (response.response != null)
						stsParams = response.response.stsParams;
					Intent stsParamsIntent = new Intent();
					stsParamsIntent.putExtra("stsParams", stsParams);
					Fragment parentFragment = getParentFragment();
					if (parentFragment != null)
						parentFragment.onActivityResult(0, SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue(), stsParamsIntent);
					break;
				default:
					progressBar.setVisibility(View.GONE);
					fragmentView.setVisibility(View.VISIBLE);
					clearVoucherCounter();
					setupErrorViewPager(viewPager);
					break;
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
		}
	}

	private void setupErrorViewPager(ViewPager viewPager) {
		if (!isAdded()) return;
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

	public void clearVoucherCounter() {
		addBadge(3, 0);
	}

	public void loadCardDetails() {
		wRewardsCardDetails = new WRewardsCardDetails(getActivity(), new OnEventListener() {
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
		if(getBottomNavigationActivity() != null && getBottomNavigationActivity().walkThroughPromtView != null){
			getBottomNavigationActivity().walkThroughPromtView.removeFromWindow();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideToolbar();
		if (wRewardsCardDetails != null && !wRewardsCardDetails.isCancelled()) {
			wRewardsCardDetails.cancel(true);
		}
	}

	public void scrollToTop() {
		if (!isAdded()) return;
		if (adapter != null) {
			Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
			switch (viewPager.getCurrentItem()) {
				case 0:
					if (page instanceof WRewardsOverviewFragment) {
						WRewardsOverviewFragment wRewardsOverviewFragment = (WRewardsOverviewFragment) page;
						wRewardsOverviewFragment.scrollToTop();
					}
					break;

				case 1:
					if (page instanceof WRewardsVouchersFragment) {
						WRewardsVouchersFragment wRewardsVouchersFragment = (WRewardsVouchersFragment) page;
						wRewardsVouchersFragment.scrollToTop();
					}
					break;

				case 2:
					if (page instanceof WRewardsSavingsFragment) {
						WRewardsSavingsFragment wRewardsSavingsFragment = (WRewardsSavingsFragment) page;
						wRewardsSavingsFragment.scrollToTop();
					}
					break;

				default:
					break;
			}
		}
	}

	public void showFeatureWalkthrough(View counterView){
		if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.vouchers)
			return;
		Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key),this.getClass().getCanonicalName());
		getBottomNavigationActivity().walkThroughPromtView = new WMaterialShowcaseView.Builder(getActivity(), WMaterialShowcaseView.Feature.VOUCHERS)
				.setTarget(counterView)
				.setTitle(R.string.tips_tricks_your_vouchers)
				.setDescription(R.string.walkthrough_vouchers_desc)
				.setActionText(R.string.tips_tricks_view_these_now)
				.setImage(R.drawable.tips_tricks_ic_coupon)
				.setAction(this)
				.setShapePadding(24)
				.setArrowPosition(WMaterialShowcaseView.Arrow.TOP_CENTER)
				.setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
		getBottomNavigationActivity().walkThroughPromtView.show(getActivity());
	}

	@Override
	public void onWalkthroughActionButtonClick() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				viewPager.setCurrentItem(1);
			}
		});
	}

	@Override
	public void onPromptDismiss() {

	}

	@Override
	public void onPause() {
		super.onPause();
		isActivityInForeground = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SIGNED_IN_LINKED);
		isActivityInForeground = true;
	}
}
