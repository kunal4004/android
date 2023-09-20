package za.co.woolworths.financial.services.android.ui.fragments.wreward;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.text.ParseException;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsSavingsHorizontalScrollAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper;
import za.co.woolworths.financial.services.android.ui.views.ScrollingLinearLayoutManager;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsSavingsFragment extends Fragment implements View.OnClickListener {
	private WRewardsSavingsHorizontalScrollAdapter mAdapter;
	private RecyclerView recyclerView;
	public VoucherResponse voucherResponse;
	public TextView wRewardsInstantSaving;
	public TextView quarterlyVoucherEarned;
	public TextView yearToDateSpend;
	public TextView yearToDateSpendText;
	public RelativeLayout noSavingsView;
	public LinearLayout savingSinceLayout;
	public TextView savingSince;
	public ImageView savingSinceInfo;
	public ImageView yearToDateSpendInfo;
	private RelativeLayout wRewardInstantSavingsRelativeLayout;
	private RelativeLayout savingSinceRelativeLayout;
	private RelativeLayout quartlyVoucherEarnedRelativeLayout;
	private RelativeLayout yearDateSpendRelativeLayout;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wrewards_savings_fragment, container, false);
		recyclerView = view.findViewById(R.id.recycler_view);
		wRewardsInstantSaving = view.findViewById(R.id.wrewardsInstantSavings);
		quarterlyVoucherEarned = view.findViewById(R.id.quarterlyVouchersEarned);
		yearToDateSpend = view.findViewById(R.id.yearToDateSpend);
		yearToDateSpendText = view.findViewById(R.id.yearToDateSpendText);
		yearToDateSpendInfo = view.findViewById(R.id.yearToDateSpendInfo);
		noSavingsView = view.findViewById(R.id.noSavingsView);
		savingSinceLayout = view.findViewById(R.id.savingSinceLayout);
		savingSince = view.findViewById(R.id.savingSince);
		savingSinceInfo = view.findViewById(R.id.savingSinceInfo);
		TextView tvWRewardInstantSaving = view.findViewById(R.id.tvWRewardInstantSaving);
		wRewardInstantSavingsRelativeLayout = view.findViewById(R.id.wRewardInstantSavingsRelativeLayout);
		savingSinceRelativeLayout = view.findViewById(R.id.savingSinceRelativeLayout);
		yearDateSpendRelativeLayout = view.findViewById(R.id.yearDateSpendRelativeLayout);
		quartlyVoucherEarnedRelativeLayout = view.findViewById(R.id.quartlyVoucherEarnedRelativeLayout);
		ScrollingLinearLayoutManager mLayoutManager = new ScrollingLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false, 1500);

		Activity activity = getActivity();
		if (activity != null) {
			tvWRewardInstantSaving.setText(WRewardBenefitActivity.Companion.convertWRewardCharacter(getString(R.string.wrewards_instant_savings)));
		}
		savingSinceInfo.setOnClickListener(this);
		yearToDateSpendInfo.setOnClickListener(this);
		recyclerView.setLayoutManager(mLayoutManager);
		Bundle bundle = getArguments();
		voucherResponse = new Gson().fromJson(bundle != null ? bundle.getString("WREWARDS") : null, VoucherResponse.class);
		if (voucherResponse.tierInfo == null) {
			displayNoSavingsView();
		} else {
			displaySavingsView();
		}
		recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getActivity(), recyclerView, new RecycleViewClickListner.ClickListener() {
			@Override
			public void onClick(View view, int position) {
				//Zero position belongs to recycleview header view
				if (position == 0) {
					setUpYearToDateValue();
				} else {
					savingSinceLayout.setVisibility(View.GONE);
					yearToDateSpendInfo.setVisibility(View.GONE);
					yearToDateSpendText.setText(getString(R.string.wrewards_monthly_spend));
					//Get data on Position-1 from Array List. And bind to UI
					wRewardsInstantSaving.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(voucherResponse.tierHistoryList.get(position - 1).monthlySavings));
					quarterlyVoucherEarned.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(voucherResponse.tierHistoryList.get(position - 1).wVouchers));
					yearToDateSpend.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(voucherResponse.tierHistoryList.get(position - 1).monthlySpend));
				}
				mAdapter.setSelectedPosition(position);
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onLongClick(View view, int position) {
			}
		}));
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		uniqueIdsForWRewardsSavings();
		WRewardUniqueLocatorsHelper.Companion.setSavingsFragLocators(view.findViewById(R.id.tvWRewardInstantSaving),
				wRewardsInstantSaving,
				view.findViewById(R.id.savingSinceTitle),
				savingSince,
				savingSinceInfo,
				view.findViewById(R.id.quarterlyVouchersEarnedTitle),
				quarterlyVoucherEarned,
				yearToDateSpend,
				yearToDateSpendText,
				yearToDateSpendInfo);
	}

	private void uniqueIdsForWRewardsSavings() {
		Activity activity = getActivity();
		if (activity != null && activity.getResources() != null) {
			noSavingsView.setContentDescription(getString(R.string.savingsLayout));
			wRewardInstantSavingsRelativeLayout.setContentDescription(getString(R.string.tvWRewardInstantSavingLayout));
			savingSinceRelativeLayout.setContentDescription(getString(R.string.savingSinceLayout));
			quartlyVoucherEarnedRelativeLayout.setContentDescription(getString(R.string.quarterlyVouchersEarnedLayout));
			yearDateSpendRelativeLayout.setContentDescription(getString(R.string.yearToDateSpendTextLayout));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_SAVINGS);
	}

	public void setUpYearToDateValue() {
		savingSinceLayout.setVisibility(View.VISIBLE);
		yearToDateSpendInfo.setVisibility(View.VISIBLE);
		yearToDateSpendText.setText(getString(R.string.year_to_date_spend));
		try {
			savingSince.setText(WFormatter.formatDate(voucherResponse.tierInfo.earnedSince));
		} catch (ParseException e) {
			e.printStackTrace();
			savingSince.setText(voucherResponse.tierInfo.earnedSince);
		}
		wRewardsInstantSaving.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(voucherResponse.tierInfo.earned));
		quarterlyVoucherEarned.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(voucherResponse.tierInfo.yearToDateWVouchers));
		yearToDateSpend.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(voucherResponse.tierInfo.yearToDateSpend));
	}

	public void displayNoSavingsView() {
		recyclerView.setVisibility(View.GONE);
		noSavingsView.setVisibility(View.VISIBLE);
		savingSinceLayout.setVisibility(View.GONE);
		yearToDateSpendText.setText(getString(R.string.year_to_date_spend));
		wRewardsInstantSaving.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(0));
		quarterlyVoucherEarned.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(0));
		yearToDateSpend.setText(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(0));

	}

	public void displaySavingsView() {
		if (voucherResponse.tierHistoryList == null || voucherResponse.tierHistoryList.size() == 0) {
			recyclerView.setVisibility(View.GONE);
			noSavingsView.setVisibility(View.VISIBLE);

		} else {
			recyclerView.setVisibility(View.VISIBLE);
			noSavingsView.setVisibility(View.GONE);
			mAdapter = new WRewardsSavingsHorizontalScrollAdapter(getActivity(), voucherResponse.tierHistoryList);
			recyclerView.setAdapter(mAdapter);
		}
		setUpYearToDateValue();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.savingSinceInfo:

			case R.id.yearToDateSpendInfo:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.INFO, getString(R.string.savings_info_message));
				break;
		}
	}

	public void scrollToTop() {
		recyclerView.smoothScrollToPosition(0);
	}
}