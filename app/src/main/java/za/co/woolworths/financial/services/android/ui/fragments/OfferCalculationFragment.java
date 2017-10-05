package za.co.woolworths.financial.services.android.ui.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;
import java.util.HashMap;

import at.grabner.circleprogress.CircleProgressView;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.rest.GetOfferAPITask;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.seekbar.CrystalSeekbar;
import za.co.woolworths.financial.services.android.ui.views.seekbar.OnSeekbarChangeListener;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class OfferCalculationFragment extends Fragment implements View.OnClickListener {

	private HashMap<String, String> mHashIncomeDetail, mHashExpenseDetail;
	private WTextView tvCurrentCreditLimitAmount, tvNewCreditLimitAmount, tvAdditionalCreditLimitAmount, tvCalculatingYourOffer, tvLoadTime;
	private ProgressBar cpCurrentCreditLimit, cpAdditionalCreditLimit, cpNewCreditAmount;
	private LinearLayout llNextButtonLayout, llSlideToEditContainer;
	private CircleProgressView mCircleView;
	private FrameLayout flCircularProgressSpinner;
	private WButton btnContinue;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.offer_calculation_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Utils.updateCLIStepIndicator(3,OfferCalculationFragment.this);
		Bundle b = this.getArguments();
		if (b.getSerializable("INCOME_DETAILS") != null) {
			mHashIncomeDetail = (HashMap<String, String>) b.getSerializable("INCOME_DETAILS");
			mHashExpenseDetail = (HashMap<String, String>) b.getSerializable("EXPENSE_DETAILS");
		}
		CreateOfferRequest createOfferRequest = new CreateOfferRequest(
				((WoolworthsApplication) OfferCalculationFragment.this.getActivity().getApplication()).getProductOfferingId(),
				1000,
				Integer.valueOf(mHashIncomeDetail.get("GROSS_MONTHLY_INCOME")),
				Integer.valueOf(mHashIncomeDetail.get("NET_MONTHLY_INCOME")),
				Integer.valueOf(mHashIncomeDetail.get("ADDITIONAL_MONTHLY_INCOME")),
				Integer.valueOf(mHashExpenseDetail.get("MORTGAGE_PAYMENTS")),
				Integer.valueOf(mHashExpenseDetail.get("RENTAL_PAYMENTS")),
				Integer.valueOf(mHashExpenseDetail.get("MAINTENANCE_EXPENSES")),
				Integer.valueOf(mHashExpenseDetail.get("MONTHLY_CREDIT_EXPENSES")), 0);
		init(view);
		onLoad();
		GetOfferAPITask getOfferAPITask = new GetOfferAPITask(getActivity(), createOfferRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				onLoadSuccess();
			}

			@Override
			public void onFailure(String e) {
			}
		});
		//getOfferAPITask.execute();

		onLoadSuccess();
	}

	private void init(View view) {

		final WTextView tvSlideToEditAmount = (WTextView) view.findViewById(R.id.tvSlideToEditAmount);
		final WTextView tvSlideToEditSeekInfo = (WTextView) view.findViewById(R.id.tvSlideToEditSeekInfo);

		tvCurrentCreditLimitAmount = (WTextView) view.findViewById(R.id.tvCurrentCreditLimitAmount);
		tvAdditionalCreditLimitAmount = (WTextView) view.findViewById(R.id.tvAdditionalCreditLimitAmount);
		tvNewCreditLimitAmount = (WTextView) view.findViewById(R.id.tvNewCreditLimitAmount);

		CrystalSeekbar sbSlideAmount = (CrystalSeekbar) view.findViewById(R.id.sbSlideAmount);
		sbSlideAmount.setMaxValue(45000);
		sbSlideAmount.setMinValue(0);
		sbSlideAmount.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
			@Override
			public void valueChanged(Number ncl) {
				String newCreditLimit = String.valueOf(ncl);
				int mCreditLimit = calculateAdditionalAmount(30000, Integer.parseInt(newCreditLimit));
				tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(mCreditLimit));
				tvSlideToEditAmount.setText(formatAmount(Integer.valueOf(newCreditLimit)));
				tvNewCreditLimitAmount.setText(formatAmount(Integer.valueOf(newCreditLimit)));
			}
		});

		sbSlideAmount.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showView(tvSlideToEditSeekInfo);
				return false;
			}
		});
		SimpleDraweeView imOfferTime = (SimpleDraweeView) view.findViewById(R.id.imOfferTime);
		flCircularProgressSpinner = (FrameLayout) view.findViewById(R.id.flCircularProgressSpinner);

		llSlideToEditContainer = (LinearLayout) view.findViewById(R.id.llSlideToEditContainer);

		mCircleView = (CircleProgressView) view.findViewById(R.id.circleView);
		mCircleView.setOnClickListener(null);
		tvCalculatingYourOffer = (WTextView) view.findViewById(R.id.tvCalculatingYourOffer);
		tvLoadTime = (WTextView) view.findViewById(R.id.tvLoadTime);

		cpCurrentCreditLimit = (ProgressBar) view.findViewById(R.id.cpCurrentCreditLimit);
		cpAdditionalCreditLimit = (ProgressBar) view.findViewById(R.id.cpAdditionalCreditLimit);
		cpNewCreditAmount = (ProgressBar) view.findViewById(R.id.cpNewCreditAmount);

		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		showView(llNextButtonLayout);

		btnContinue = (WButton) view.findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(this);
		btnContinue.setText(getActivity().getResources().getString(R.string.accept_offer));
		showView(btnContinue);


		try {
			new DrawImage(getActivity()).handleGIFImage(imOfferTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String additionalAmountSignSum(int additionalCreditLimit) {
		String strAdditionalCreditLimit;
		switch ((int) Math.signum(additionalCreditLimit)) {
			case 0:
				strAdditionalCreditLimit = formatAmount(additionalCreditLimit);
				break;
			case 1:
				strAdditionalCreditLimit = "+ " + formatAmount(additionalCreditLimit);
				break;
			case -1:
				additionalCreditLimit = Math.abs(additionalCreditLimit);
				strAdditionalCreditLimit = "- " + formatAmount(additionalCreditLimit);
				break;
			default:
				additionalCreditLimit = Math.abs(additionalCreditLimit);
				strAdditionalCreditLimit = formatAmount(additionalCreditLimit);
				break;
		}
		return strAdditionalCreditLimit;
	}

	private String formatAmount(int amount) {
		return WFormatter.amountFormat(amount * 100);
	}

	private void onLoad() {
		getCLIText(tvCalculatingYourOffer, R.string.calculating_your_offer);
		getCLIText(tvLoadTime, R.string.amount_of_time_info);
		hideView(tvCurrentCreditLimitAmount);
		hideView(tvAdditionalCreditLimitAmount);
		hideView(llSlideToEditContainer);
		hideView(tvNewCreditLimitAmount);
		showView(cpCurrentCreditLimit);
		showView(cpAdditionalCreditLimit);
		showView(cpNewCreditAmount);
		showView(flCircularProgressSpinner);
		progressColorFilter(cpCurrentCreditLimit, Color.BLACK);
		progressColorFilter(cpAdditionalCreditLimit, Color.BLACK);
		progressColorFilter(cpNewCreditAmount, Color.BLACK);
		calculateOfferProgress();
		disableView(btnContinue);
	}

	private void calculateOfferProgress() {
		// value setting
		mCircleView.setMaxValue(100);
		mCircleView.setValue(0);
		mCircleView.setValueAnimated(24);
		mCircleView.setSpinningBarLength(150);
		mCircleView.spin(); // start spinning
	}

	private void onLoadSuccess() {
		getCLIText(tvCalculatingYourOffer, R.string.pre_approved_for_title);
		getCLIText(tvLoadTime, R.string.subject_to_proof_of_income);
		showView(tvCurrentCreditLimitAmount);
		showView(tvAdditionalCreditLimitAmount);
		showView(llSlideToEditContainer);
		showView(tvNewCreditLimitAmount);
		hideView(cpCurrentCreditLimit);
		hideView(cpAdditionalCreditLimit);
		hideView(cpNewCreditAmount);
		hideView(flCircularProgressSpinner);
		mCircleView.stopSpinning();
		enableView(btnContinue);
	}

	private void getCLIText(WTextView wTextView, int id) {
		wTextView.setText(getActivity().getResources().getString(id));
	}

	private void disableView(View v) {
		v.setEnabled(false);
	}

	private void enableView(View v) {
		v.setEnabled(true);
	}

	private void hideView(View v) {
		v.setVisibility(View.GONE);
	}

	private void showView(View v) {
		v.setVisibility(View.VISIBLE);
	}

	private void setBackgroundColor(View v, int id) {
		v.setBackgroundColor(ContextCompat.getColor(getActivity(), id));
	}

	private int calculateAdditionalAmount(int currentCreditLimit, int NewCreditLimit) {
		return currentCreditLimit - NewCreditLimit;
	}

	private void progressColorFilter(ProgressBar progressBar, int color) {
		progressBar.setIndeterminate(true);
		progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnContinue:
				/*CLISupplyDocumentsFragment fragment=new CLISupplyDocumentsFragment();
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
						.replace(R.id.cli_steps_container, fragment).addToBackStack(null).commit();*/
				DocumentFragment documentFragment = new DocumentFragment();
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
						.replace(R.id.cli_steps_container, documentFragment).addToBackStack(null).commit();
				break;
		}
	}

}