package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.rest.CLICreateOffer;
import za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.views.CircleProgressBar;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;
import za.co.woolworths.financial.services.android.util.tooltip.TooltipHelper;
import za.co.woolworths.financial.services.android.util.tooltip.ViewTooltip;

public class OfferCalculationFragment extends CLIFragment implements View.OnClickListener, NetworkChangeListener {

	private static final int INCREASE_PROGRESS_BY = 100;
	private HashMap<String, String> mHashIncomeDetail, mHashExpenseDetail;
	private WTextView tvCurrentCreditLimitAmount, tvNewCreditLimitAmount, tvAdditionalCreditLimitAmount, tvCalculatingYourOffer, tvLoadTime, tvSlideToEditSeekInfo, tvSlideToEditAmount;
	private ProgressBar cpCurrentCreditLimit, cpAdditionalCreditLimit, cpNewCreditAmount;
	private LinearLayout llSlideToEditContainer, llNextButtonLayout;
	private CircleProgressBar mCircleView;
	private FrameLayout flCircularProgressSpinner;
	private WButton btnContinue;
	private SeekBar sbSlideAmount;
	private int mCreditReqestMin = 0;
	private int mDifferenceCreditLimit = 0;
	private int mCurrentCredit = 0;
	private CreateOfferResponse mObjOffer;
	private CLIPhase2Activity mCliPhase2Activity;
	private ProgressBar mAcceptOfferProgressBar;
	private BroadcastReceiver mConnectionBroadcast;
	private boolean loadCompleted = false;
	private ErrorHandlerView mErrorHandlerView;
	private ViewTooltip mTooltip;


	private enum LATEST_BACKGROUND_CALL {CREATE_OFFER, DECLINE_OFFER, ACCEPT_OFFER}

	private LATEST_BACKGROUND_CALL latest_background_call;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.offer_calculation_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null) {
			mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
			init(view);
			onLoad();
			seekBar();
			Bundle b = this.getArguments();
			Serializable incomeDetail = b.getSerializable(IncreaseLimitController.INCOME_DETAILS);
			Serializable expenseDetail = b.getSerializable(IncreaseLimitController.EXPENSE_DETAILS);
			setInvisibleView(tvSlideToEditSeekInfo);
			cliStepIndicatorListener.onStepSelected(3);
			mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
			if (incomeDetail != null) {
				mHashIncomeDetail = (HashMap<String, String>) incomeDetail;
			}
			if (expenseDetail != null) {
				mHashExpenseDetail = (HashMap<String, String>) expenseDetail;
				mObjOffer = ((CLIPhase2Activity) getActivity()).offerActiveObject();
				displayCurrentOffer(mObjOffer);
			} else {
				cliCreateOfferRequest(createOffer(mHashIncomeDetail, mHashExpenseDetail));
			}

			TooltipHelper tooltipHelper = new TooltipHelper(getActivity());
			mTooltip = tooltipHelper.showToolTipView(sbSlideAmount, getString(R.string.slide_to_edit_amount),
					ContextCompat.getColor(getActivity(), R.color.tooltip_bg_color));
			mTooltip.show();
		}
	}

	private void seekBar() {
		sbSlideAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				progress = progress / INCREASE_PROGRESS_BY;
				progress = progress * INCREASE_PROGRESS_BY;
				int newCLIAmount = mCreditReqestMin + progress;
				tvSlideToEditAmount.setText(formatAmount(newCLIAmount));
				tvNewCreditLimitAmount.setText(tvSlideToEditAmount.getText().toString());
				tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(calculateAdditionalAmount(mCurrentCredit, tvNewCreditLimitAmount.getText().toString())));
			}
		});
	}

	private void cliCreateOfferRequest(CreateOfferRequest createOfferRequest) {
		latestBackgroundTask(LATEST_BACKGROUND_CALL.CREATE_OFFER);
		CLICreateOffer getOfferAPITask = new CLICreateOffer(getActivity(), createOfferRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				mObjOffer = ((CreateOfferResponse) object);
				int httpCode = mObjOffer.httpCode;
				switch (httpCode) {
					case 200:
						displayCurrentOffer(mObjOffer);
						break;

					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
						break;

					default:
						Utils.displayValidationMessage(getActivity(), CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.ERROR, mObjOffer.response.desc);
						break;
				}
				onLoadCompleted(true);
				onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				onLoadCompleted(false);
			}
		});
		getOfferAPITask.execute();
	}

	private void init(View view) {

		mCliPhase2Activity = (CLIPhase2Activity) getActivity();
		tvSlideToEditSeekInfo = (WTextView) view.findViewById(R.id.tvSlideToEditSeekInfo);
		mAcceptOfferProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);

		tvCurrentCreditLimitAmount = (WTextView) view.findViewById(R.id.tvCurrentCreditLimitAmount);
		tvAdditionalCreditLimitAmount = (WTextView) view.findViewById(R.id.tvAdditionalCreditLimitAmount);
		tvNewCreditLimitAmount = (WTextView) view.findViewById(R.id.tvNewCreditLimitAmount);
		tvSlideToEditAmount = (WTextView) view.findViewById(R.id.tvSlideToEditAmount);

		sbSlideAmount = (SeekBar) view.findViewById(R.id.sbSlideAmount);

		SimpleDraweeView imOfferTime = (SimpleDraweeView) view.findViewById(R.id.imOfferTime);
		flCircularProgressSpinner = (FrameLayout) view.findViewById(R.id.flCircularProgressSpinner);

		llSlideToEditContainer = (LinearLayout) view.findViewById(R.id.llSlideToEditContainer);

		mCircleView = (CircleProgressBar) view.findViewById(R.id.circleView);
		tvCalculatingYourOffer = (WTextView) view.findViewById(R.id.tvCalculatingYourOffer);
		tvLoadTime = (WTextView) view.findViewById(R.id.tvLoadTime);

		cpCurrentCreditLimit = (ProgressBar) view.findViewById(R.id.cpCurrentCreditLimit);
		cpAdditionalCreditLimit = (ProgressBar) view.findViewById(R.id.cpAdditionalCreditLimit);
		cpNewCreditAmount = (ProgressBar) view.findViewById(R.id.cpNewCreditAmount);

		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		showView(llNextButtonLayout);

		btnContinue = (WButton) view.findViewById(R.id.btnContinue);
		btnContinue.setOnClickListener(this);
		tvSlideToEditAmount.setOnClickListener(this);
		btnContinue.setText(getActivity().getResources().getString(R.string.accept_offer));
		showView(btnContinue);
		mCliPhase2Activity.showDeclineOffer();

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
		spinProgress(mCircleView);
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
		disableView(btnContinue);
		setBackgroundColor(llNextButtonLayout, R.color.white);
	}

	private void onLoadComplete() {
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
		enableView(btnContinue);
		setBackgroundColor(llNextButtonLayout, R.color.default_background);
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

	private void onAcceptOfferLoad() {
		showView(mAcceptOfferProgressBar);
		mAcceptOfferProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		hideView(btnContinue);
	}

	private void onAcceptOfferCompleted() {
		hideView(mAcceptOfferProgressBar);
		showView(btnContinue);
	}


	private void setBackgroundColor(View v, int id) {
		v.setBackgroundColor(ContextCompat.getColor(getActivity(), id));
	}

	private int calculateAdditionalAmount(int currentCreditLimit, String newCreditLimit) {
		int mNewCreditLimit = Utils.numericFieldOnly(newCreditLimit);
		return mNewCreditLimit - currentCreditLimit;
	}

	private void progressColorFilter(ProgressBar progressBar, int color) {
		progressBar.setIndeterminate(true);
		progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnContinue:
				onAcceptOfferLoad();
				latestBackgroundTask(LATEST_BACKGROUND_CALL.ACCEPT_OFFER);
				int newCreditLimitAmount = Utils.numericFieldOnly(tvNewCreditLimitAmount.getText().toString());
				CreateOfferDecision createOfferDecision = new CreateOfferDecision(String.valueOf(mObjOffer.cliOfferId), "Accept", String.valueOf(newCreditLimitAmount));
				CLIOfferDecision cliOfferDecision =
						new CLIOfferDecision(getActivity(), createOfferDecision, new OnEventListener() {

							@Override
							public void onSuccess(Object object) {
								mObjOffer = ((CreateOfferResponse) object);
								switch (mObjOffer.httpCode) {
									case 200:

										DocumentFragment documentFragment = new DocumentFragment();
										documentFragment.setStepIndicatorListener(cliStepIndicatorListener);
										FragmentUtils fragmentUtils = new FragmentUtils();
										fragmentUtils.nextFragment((AppCompatActivity) OfferCalculationFragment.this.getActivity(), getFragmentManager().beginTransaction(), documentFragment, R.id.cli_steps_container);
										break;
									case 440:
										SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
										break;
									default:
										Utils.displayValidationMessage(getActivity(), CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.ERROR, mObjOffer.response.desc);
										break;
								}
								onLoadCompleted(true);
								onAcceptOfferCompleted();
							}

							@Override
							public void onFailure(String e) {
								onLoadCompleted(false);
								onAcceptOfferCompleted();
							}
						});

				cliOfferDecision.execute();
				break;

			case R.id.tvSlideToEditAmount:
				try {
					Bundle args = new Bundle();
					String slideAmount = tvSlideToEditAmount.getText().toString();
					if (slideAmount != null) {
						args.putInt("slideAmount", Integer.valueOf(Utils.numericFieldOnly(slideAmount)));
					}

					args.putInt("currCredit", mObjOffer.cli.offer.currCredit);
					args.putInt("creditReqestMin", mObjOffer.cli.offer.creditReqestMin);
					args.putInt("creditRequestMax", mObjOffer.cli.offer.creditRequestMax);

					mCliPhase2Activity.actionBarBackIcon();
					EditAmountFragment editAmountFragment = new EditAmountFragment();
					editAmountFragment.setArguments(args);
					editAmountFragment.setStepIndicatorListener(cliStepIndicatorListener);
					FragmentUtils fragmentUtils = new FragmentUtils(false);
					fragmentUtils.nextFragment((AppCompatActivity) OfferCalculationFragment.this.getActivity(), getFragmentManager().beginTransaction(), editAmountFragment, R.id.cli_steps_container);
				} catch (NullPointerException ignored) {
				}

				break;
		}
	}

	public CreateOfferRequest createOffer(HashMap<String, String> hashIncomeDetail, HashMap<String, String> hashExpenseDetail) {
		return new CreateOfferRequest(
				((WoolworthsApplication) getActivity().getApplication()).getProductOfferingId(),
				1000,
				Integer.valueOf(hashIncomeDetail.get("GROSS_MONTHLY_INCOME")),
				Integer.valueOf(hashIncomeDetail.get("NET_MONTHLY_INCOME")),
				Integer.valueOf(hashIncomeDetail.get("ADDITIONAL_MONTHLY_INCOME")),
				Integer.valueOf(hashExpenseDetail.get("MORTGAGE_PAYMENTS")),
				Integer.valueOf(hashExpenseDetail.get("RENTAL_PAYMENTS")),
				Integer.valueOf(hashExpenseDetail.get("MAINTENANCE_EXPENSES")),
				Integer.valueOf(hashExpenseDetail.get("MONTHLY_CREDIT_EXPENSES")),
				Integer.valueOf(hashExpenseDetail.get("OTHER_EXPENSES")));
	}

	private void spinProgress(CircleProgressBar progressBar) {
		final ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
		animation.setDuration(5000);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				//do something when the countdown is complete
			}

			@Override
			public void onAnimationCancel(Animator animator) {
			}

			@Override
			public void onAnimationRepeat(Animator animator) {
			}
		});
		animation.setRepeatCount(1000);
		animation.start();
	}

	public void displayCurrentOffer(CreateOfferResponse mObjOffer) {
		Offer offer = mObjOffer.cli.offer;
		mCurrentCredit = offer.currCredit;
		mCreditReqestMin = offer.creditReqestMin;
		int creditRequestMax = offer.creditRequestMax;
		mDifferenceCreditLimit = (creditRequestMax - mCreditReqestMin);

		sbSlideAmount.setMax(mDifferenceCreditLimit);
		sbSlideAmount.incrementProgressBy(INCREASE_PROGRESS_BY);
		sbSlideAmount.setProgress(mDifferenceCreditLimit);

		tvCurrentCreditLimitAmount.setText(formatAmount(mCurrentCredit));
		tvNewCreditLimitAmount.setText(tvSlideToEditAmount.getText().toString());
		tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(calculateAdditionalAmount(mCurrentCredit, tvNewCreditLimitAmount.getText().toString())));
		int newCreditLimitAmount = Utils.numericFieldOnly(tvNewCreditLimitAmount.getText().toString());
		((WoolworthsApplication) OfferCalculationFragment.this.getActivity().getApplication()).getWGlobalState().setDecisionDeclineOffer(new CreateOfferDecision(String.valueOf(mObjOffer.cliOfferId), "Decline", String.valueOf(newCreditLimitAmount)));
		onLoadComplete();
	}

	public void setInvisibleView(View invisibleView) {
		invisibleView.setVisibility(View.INVISIBLE);
	}

	private void latestBackgroundTask(LATEST_BACKGROUND_CALL latest_background_call) {
		this.latest_background_call = latest_background_call;
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mConnectionBroadcast);
	}

	@Override
	public void onResume() {
		super.onResume();
//		int editNumberFromUser = mCliPhase2Activity.getEditNumberValue();
//		if (!TextUtils.isEmpty(String.valueOf(editNumberFromUser))) {
//			sbSlideAmount.setProgress(editNumberFromUser);
//		}
		mCliPhase2Activity.actionBarCloseIcon();
		getActivity().registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public void onConnectionChanged() {
		retryConnect();
	}

	private void retryConnect() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (new ConnectionDetector().isOnline(getActivity())) {
					if (!loadCompleted) {
						if (latest_background_call != null) {
							switch (latest_background_call) {
								case CREATE_OFFER:
									break;

								case ACCEPT_OFFER:
									btnContinue.performClick();
									break;

								default:
									break;
							}
						}
					}
				} else {
//					mErrorHandlerView.showToast();
					Log.e("ConnectionDetector", "showToast");
					switch (latest_background_call) {
						case CREATE_OFFER:
							break;

						case ACCEPT_OFFER:
							mErrorHandlerView.showToast();
							break;

						default:
							break;
					}
				}
			}
		});
	}

	private void onLoadCompleted(boolean value) {
		loadCompleted = value;
	}
}