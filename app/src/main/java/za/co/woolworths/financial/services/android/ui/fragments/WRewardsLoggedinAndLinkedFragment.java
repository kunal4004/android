package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsErrorFragment;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.util.AlertDialogInterface;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.AlertDialogManager;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedinAndLinkedFragment extends Fragment implements AlertDialogInterface {

	private MenuNavigationInterface mNavigationInterface;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private WRewardsFragmentPagerAdapter adapter;
	private ProgressBar progressBar;
	private LinearLayout fragmentView;
	private ErrorHandlerView mErrorHandlerView;
	private RelativeLayout mRlConnect;
	private AlertDialogManager mTokenExpireDialog;
	private WRewardsLoggedinAndLinkedFragment mContext;
	private WGlobalState mWGlobalState;
	private HttpAsyncTask<String, String, VoucherResponse> asyncTaskReward;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mContext = this;
		return inflater.inflate(R.layout.wrewards_loggedin_and_linked_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mWGlobalState = ((WoolworthsApplication) getActivity().getApplication()).getWGlobalState();
		viewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mNavigationInterface = (MenuNavigationInterface) getActivity();
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		fragmentView = (LinearLayout) view.findViewById(R.id.fragmentView);
		tabLayout = (TabLayout) view.findViewById(R.id.tabs);
		viewPager.setOffscreenPageLimit(3);
		progressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		mRlConnect = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), mRlConnect);
		mErrorHandlerView.setMargin(mRlConnect, 0, 0, 0, 0);
		mTokenExpireDialog = new AlertDialogManager(getActivity(), (WoolworthsApplication) getActivity().getApplication(), mContext);
		loadReward();
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(getActivity())) {
					mNavigationInterface.switchToView(3);
				}
			}
		});
	}

	private void setupViewPager(ViewPager viewPager, VoucherResponse voucherResponse) {
		Bundle bundle = new Bundle();
		bundle.putString("WREWARDS", Utils.objectToJson(voucherResponse));
		adapter = new WRewardsFragmentPagerAdapter(getChildFragmentManager(), bundle);
		adapter.addFrag(new WRewardsOverviewFragment(), getString(R.string.overview));
		adapter.addFrag(new WRewardsVouchersFragment(), getString(R.string.vouchers));
		adapter.addFrag(new WRewardsSavingsFragment(), getString(R.string.savings));
		viewPager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		tabLayout.setupWithViewPager(viewPager);
		viewPager.invalidate();
		setupTabIcons(voucherResponse.voucherCollection.vouchers.size());

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
		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
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
		asyncTaskReward = getWRewards();
		asyncTaskReward.execute();
	}

	public HttpAsyncTask<String, String, VoucherResponse> getWRewards() {
		return new HttpAsyncTask<String, String, VoucherResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressBar.setVisibility(View.VISIBLE);
				mErrorHandlerView.hideErrorHandlerLayout();
			}

			@Override
			protected VoucherResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getVouchers();
			}

			@Override
			protected Class<VoucherResponse> httpDoInBackgroundReturnType() {
				return VoucherResponse.class;
			}

			@Override
			protected VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new VoucherResponse();
			}

			@Override
			protected void onPostExecute(VoucherResponse voucherResponse) {
				super.onPostExecute(voucherResponse);
				progressBar.setVisibility(View.GONE);
				fragmentView.setVisibility(View.VISIBLE);
				handleVoucherResponse(voucherResponse);
			}
		};
	}

	public void handleVoucherResponse(VoucherResponse voucherResponse) {
		try {
			switch (voucherResponse.httpCode) {
				case 200:
					mWGlobalState.setRewardSignInState(true);
					setupViewPager(viewPager, voucherResponse);
					break;
				case 440:
					mWGlobalState.setRewardSignInState(false);
					mTokenExpireDialog.showExpiredTokenDialog(voucherResponse.response.stsParams);
					break;
				default:
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
	public void onExpiredTokenCancel() {
		if (!asyncTaskReward.isCancelled()) {
			asyncTaskReward.cancel(true);
		}
		mWGlobalState.setRewardSignInState(false);
		Intent intent = new Intent();
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
		getFragmentManager().popBackStack();
	}

	@Override
	public void onExpiredTokenAuthentication() {
		mTokenExpireDialog.reAuthenticate();
	}
}
