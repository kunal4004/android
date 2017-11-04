package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Application;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.CLICreateOffer;
import za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision;
import za.co.woolworths.financial.services.android.models.rest.CLIUpdateApplication;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
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
import za.co.woolworths.financial.services.android.util.controller.EventStatus;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class OfferCalculationFragment extends CLIFragment implements View.OnClickListener, NetworkChangeListener {

	private static final int INCREASE_PROGRESS_BY = 100;
	private static final int SLIDE_ANIM_DURATION = 1500;
	private HashMap<String, String> mHashIncomeDetail, mHashExpenseDetail;
	private WTextView tvCurrentCreditLimitAmount, tvNewCreditLimitAmount, tvAdditionalCreditLimitAmount, tvCalculatingYourOffer, tvLoadTime, tvSlideToEditSeekInfo, tvSlideToEditAmount;
	private ProgressBar cpCurrentCreditLimit, cpAdditionalCreditLimit, cpNewCreditAmount;
	private LinearLayout llSlideToEditContainer, llNextButtonLayout;
	private FrameLayout flCircularProgressSpinner;
	private WButton btnContinue;
	private SeekBar sbSlideAmount;
	private int mCreditReqestMin = 0, mDifferenceCreditLimit = 0, mCurrentCredit = 0;
	private OfferActive mObjOffer;
	private CLIPhase2Activity mCliPhase2Activity;
	private ProgressBar mAcceptOfferProgressBar;
	private BroadcastReceiver mConnectionBroadcast;
	private boolean loadCompleted = false;
	private ErrorHandlerView mErrorHandlerView;
	private RelativeLayout relConnectionLayout;
	private boolean fromOfferActive, editorWasShown;
	private WoolworthsApplication mWoolies;
	private boolean mAlreadyLoaded = false;
	private int mCLiId;
	private WGlobalState mGlobalState;
	private RelativeLayout mRelNextButton;
	private FrameLayout flTopLayout;
	private LinearLayout llEmptyLayout;
	private EventStatus mEventStatus;

	private enum LATEST_BACKGROUND_CALL {CREATE_OFFER, DECLINE_OFFER, UPDATE_APPLICATION, ACCEPT_OFFER}

	private LATEST_BACKGROUND_CALL latest_background_call;

	private View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.offer_calculation_fragment, container, false);
			latestBackgroundTask(LATEST_BACKGROUND_CALL.CREATE_OFFER);
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null && !mAlreadyLoaded) {
			mAlreadyLoaded = true;
			mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
			init(view);
			seekBar();
			listener();
			mWoolies = ((WoolworthsApplication) getActivity().getApplication());
			mGlobalState = mWoolies.getWGlobalState();
			Bundle bundle = this.getArguments();
			if (!editorWasShown) {
				onLoad();
				Serializable incomeDetail = null, expenseDetail = null;
				if (bundle != null) {
					incomeDetail = bundle.getSerializable(IncreaseLimitController.INCOME_DETAILS);
					expenseDetail = bundle.getSerializable(IncreaseLimitController.EXPENSE_DETAILS);
				}
				setInvisibleView(tvSlideToEditSeekInfo);
				cliStepIndicatorListener.onStepSelected(3);
				mObjOffer = ((CLIPhase2Activity) OfferCalculationFragment.this.getActivity()).offerActiveObject();
				if (incomeDetail != null) {
					mHashIncomeDetail = (HashMap<String, String>) incomeDetail;
				}
				if (expenseDetail != null) {
					mHashExpenseDetail = (HashMap<String, String>) expenseDetail;
					Activity activity = getActivity();
					if (activity instanceof CLIPhase2Activity) {
						mEventStatus = ((CLIPhase2Activity) activity).getEventStatus();
						if (mEventStatus == null) {
							mEventStatus = EventStatus.NONE;
						}
						createUpdateOfferTask(mEventStatus);
					}
				}
			}
		}
	}

	private void listener() {
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(getActivity())) {
					showView(llNextButtonLayout);
					mErrorHandlerView.hideErrorHandlerLayout();
					createUpdateOfferTask(mEventStatus);
				}
			}
		});
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
				String amount = tvSlideToEditAmount.getText().toString();
				tvNewCreditLimitAmount.setText(amount);
				mGlobalState.setCreditLimit(amount);
				tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(calculateAdditionalAmount(mCurrentCredit, tvNewCreditLimitAmount.getText().toString())));
			}
		});
	}

	private void cliCreateOfferRequest(CreateOfferRequest createOfferRequest) {
		onLoad();
		latestBackgroundTask(LATEST_BACKGROUND_CALL.CREATE_OFFER);
		CLICreateOffer createOfferTask = new CLICreateOffer(getActivity(), createOfferRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				mObjOffer = ((OfferActive) object);
				int httpCode = mObjOffer.httpCode;
				switch (httpCode) {
					case 200:
						displayCurrentOffer(mObjOffer);
						break;

					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
						break;

					default:
						Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, mObjOffer.response.desc);
						break;
				}
				onLoadCompleted(true);
				onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideView(llNextButtonLayout);
						onLoadCompleted(false);
						mErrorHandlerView.showErrorHandler();
					}
				});
			}
		});
		createOfferTask.execute();
	}

	private void cliUpdateApplicationTask(CreateOfferRequest createOfferRequest, String cliId) {
		onLoad();
		latestBackgroundTask(LATEST_BACKGROUND_CALL.UPDATE_APPLICATION);
		CLIUpdateApplication cliUpdateApplication = new CLIUpdateApplication(getActivity(), createOfferRequest, cliId, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				mObjOffer = ((OfferActive) object);
				int httpCode = mObjOffer.httpCode;
				switch (httpCode) {
					case 200:
						displayCurrentOffer(mObjOffer);
						break;

					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
						break;

					default:
						Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, mObjOffer.response.desc);
						break;
				}
				onLoadCompleted(true);
				onLoadComplete();
			}

			@Override
			public void onFailure(final String e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						onLoadCompleted(false);
						hideView(llNextButtonLayout);
						mErrorHandlerView.showErrorHandler();
					}
				});
			}
		});
		cliUpdateApplication.execute();
	}

	private void init(View view) {

		mCliPhase2Activity = (CLIPhase2Activity) getActivity();
		mRelNextButton = (RelativeLayout) view.findViewById(R.id.relNextButton);
		tvSlideToEditSeekInfo = (WTextView) view.findViewById(R.id.tvSlideToEditSeekInfo);
		mAcceptOfferProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
		SimpleDraweeView imOfferTime = (SimpleDraweeView) view.findViewById(R.id.imOfferTime);
		relConnectionLayout = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		flTopLayout = (FrameLayout) view.findViewById(R.id.flTopLayout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relConnectionLayout);
		mErrorHandlerView.setMargin(relConnectionLayout, 0, 0, 0, 0);
		tvCurrentCreditLimitAmount = (WTextView) view.findViewById(R.id.tvCurrentCreditLimitAmount);
		tvAdditionalCreditLimitAmount = (WTextView) view.findViewById(R.id.tvAdditionalCreditLimitAmount);
		tvNewCreditLimitAmount = (WTextView) view.findViewById(R.id.tvNewCreditLimitAmount);
		tvSlideToEditAmount = (WTextView) view.findViewById(R.id.tvSlideToEditAmount);

		sbSlideAmount = (SeekBar) view.findViewById(R.id.sbSlideAmount);

		flCircularProgressSpinner = (FrameLayout) view.findViewById(R.id.flCircularProgressSpinner);

		llSlideToEditContainer = (LinearLayout) view.findViewById(R.id.llSlideToEditContainer);
		llEmptyLayout = (LinearLayout) view.findViewById(R.id.llEmptyLayout);
		IncreaseLimitController increaseLimitController = new IncreaseLimitController(getActivity());
		increaseLimitController.setQuarterHeight(llEmptyLayout);

		tvCalculatingYourOffer = (WTextView) view.findViewById(R.id.tvCalculatingYourOffer);
		tvLoadTime = (WTextView) view.findViewById(R.id.tvLoadTime);

		cpCurrentCreditLimit = (ProgressBar) view.findViewById(R.id.cpCurrentCreditLimit);
		cpAdditionalCreditLimit = (ProgressBar) view.findViewById(R.id.cpAdditionalCreditLimit);
		cpNewCreditAmount = (ProgressBar) view.findViewById(R.id.cpNewCreditAmount);
		llNextButtonLayout = (LinearLayout) view.findViewById(R.id.llNextButtonLayout);
		showView(llNextButtonLayout);
		disableView(llNextButtonLayout);
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
		setBackgroundColor(llEmptyLayout, R.color.white);
		setBackgroundColor(llNextButtonLayout, R.color.white);
		setBackgroundColor(flTopLayout, R.color.white);
		disableView(mRelNextButton);
		disableView(btnContinue);
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
		showView(llNextButtonLayout);
		progressColorFilter(cpCurrentCreditLimit, Color.BLACK);
		progressColorFilter(cpAdditionalCreditLimit, Color.BLACK);
		progressColorFilter(cpNewCreditAmount, Color.BLACK);
	}

	private void onLoadComplete() {
		setBackgroundColor(llEmptyLayout, R.color.default_background);
		setBackgroundColor(flTopLayout, R.color.default_background);
		setBackgroundColor(llNextButtonLayout, R.color.default_background);
		enableView(mRelNextButton);
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
		enableView(llNextButtonLayout);
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
		btnContinue.setTextColor(Color.WHITE);
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
				CreateOfferDecision createOfferDecision = new CreateOfferDecision(mWoolies.getProductOfferingId(), mCLiId
						, IncreaseLimitController.ACCEPT, newCreditLimitAmount);
				CLIOfferDecision cliOfferDecision =
						new CLIOfferDecision(getActivity(), createOfferDecision, String.valueOf(mCLiId), new OnEventListener() {
							@Override
							public void onSuccess(Object object) {
								mObjOffer = ((OfferActive) object);
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
										Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, mObjOffer.response.desc);
										break;
								}
								onLoadCompleted(true);
								onAcceptOfferCompleted();
							}

							@Override
							public void onFailure(String e) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										onLoadCompleted(false);
										onAcceptOfferCompleted();
									}
								});
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

					args.putInt("currCredit", mObjOffer.offer.currCredit);
					args.putInt("creditReqestMin", mObjOffer.offer.creditReqestMin);
					args.putInt("creditRequestMax", mObjOffer.offer.creditRequestMax);

					mCliPhase2Activity.actionBarBackIcon();
					EditAmountFragment editAmountFragment = new EditAmountFragment();
					editAmountFragment.setArguments(args);
					editAmountFragment.setStepIndicatorListener(cliStepIndicatorListener);
					FragmentUtils ftils = new FragmentUtils(false);
					ftils.nextFragment((AppCompatActivity) OfferCalculationFragment.this.getActivity(), getFragmentManager().beginTransaction(), editAmountFragment, R.id.cli_steps_container);

					editorWasShown = true;
				} catch (NullPointerException ignored) {
				}

				break;
		}
	}

	public CreateOfferRequest createOffer
			(HashMap<String, String> hashIncomeDetail, HashMap<String, String> hashExpenseDetail) {
		Application application = mObjOffer.application;
		return new CreateOfferRequest(
				application.channel,
				application.debtDisclosed,
				application.canObtainCreditInfo,
				application.canObtainBankStatements,
				application.applicationInfoIsCorrect,
				application.staffMember,
				application.automaticCreditIncrease,
				application.maxCreditLimitRequested,
				mWoolies.getProductOfferingId(),
				1000,
				Integer.valueOf(hashIncomeDetail.get("NET_MONTHLY_INCOME")),
				Integer.valueOf(hashIncomeDetail.get("ADDITIONAL_MONTHLY_INCOME")),
				Integer.valueOf(hashExpenseDetail.get("MORTGAGE_PAYMENTS")),
				Integer.valueOf(hashIncomeDetail.get("GROSS_MONTHLY_INCOME")),
				Integer.valueOf(hashExpenseDetail.get("RENTAL_PAYMENTS")),
				Integer.valueOf(hashExpenseDetail.get("MAINTENANCE_EXPENSES")),
				Integer.valueOf(hashExpenseDetail.get("MONTHLY_CREDIT_EXPENSES")),
				Integer.valueOf(hashExpenseDetail.get("OTHER_EXPENSES")
				));
	}

	public void displayCurrentOffer(OfferActive mObjOffer) {
		Offer offer = mObjOffer.offer;
		mCurrentCredit = offer.currCredit;
		mCreditReqestMin = offer.creditReqestMin;
		int creditRequestMax = offer.creditRequestMax;
		mDifferenceCreditLimit = (creditRequestMax - mCreditReqestMin);
		mCLiId = mObjOffer.cliId;
		sbSlideAmount.setMax(mDifferenceCreditLimit);
		sbSlideAmount.incrementProgressBy(INCREASE_PROGRESS_BY);
		animSeekBarToMaximum();
		tvCurrentCreditLimitAmount.setText(formatAmount(mCurrentCredit));
		tvNewCreditLimitAmount.setText(tvSlideToEditAmount.getText().toString());
		tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(calculateAdditionalAmount(mCurrentCredit, tvNewCreditLimitAmount.getText().toString())));
		int newCreditLimitAmount = Utils.numericFieldOnly(tvNewCreditLimitAmount.getText().toString());
		int cliId = mObjOffer.cliId;
		mGlobalState.setDecisionDeclineOffer(new CreateOfferDecision(mWoolies.getProductOfferingId(), cliId, IncreaseLimitController.DECLINE, newCreditLimitAmount));
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
		if (editorWasShown) {
			int editNumberFromUser = mCliPhase2Activity.getEditNumberValue();
			if (!TextUtils.isEmpty(String.valueOf(editNumberFromUser)) && !(editNumberFromUser == -1)) {
				sbSlideAmount.setProgress(editNumberFromUser);
			}
			editorWasShown = false;
		}
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
								case DECLINE_OFFER:
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
					switch (latest_background_call) {
						case DECLINE_OFFER:
							if (fromOfferActive) {
								hideView(llNextButtonLayout);
								mErrorHandlerView.showErrorHandler();
							}
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

	public void animSeekBarToMaximum() {
		final ValueAnimator anim = ValueAnimator.ofInt(0, sbSlideAmount.getMax());
		anim.setDuration(SLIDE_ANIM_DURATION);
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int animProgress = (Integer) animation.getAnimatedValue();
				sbSlideAmount.setProgress(animProgress);
			}

		});
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				Utils.showOneTimeTooltip(getActivity(), SessionDao.KEY.CLI_SLIDE_EDIT_AMOUNT_TOOLTIP, sbSlideAmount, getString(R.string.slide_to_edit_amount));
			}
		});
		anim.start();
	}

	public void createUpdateOfferTask(EventStatus eventStatus) {
		switch (eventStatus) {
			case CREATE_OFFER:
				cliCreateOfferRequest(createOffer(mHashIncomeDetail, mHashExpenseDetail));
				break;

			case UPDATE_OFFER:
				cliUpdateApplicationTask(createOffer(mHashIncomeDetail, mHashExpenseDetail), String.valueOf(mObjOffer.cliId));
				break;
			default:
				displayCurrentOffer(mObjOffer);
				fromOfferActive = true;
				break;
		}
	}
}