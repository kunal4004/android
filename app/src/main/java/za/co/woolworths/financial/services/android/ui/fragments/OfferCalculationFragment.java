package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.CLICreateApplication;
import za.co.woolworths.financial.services.android.models.rest.CLIUpdateApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.DeclineOfferInterface;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.EventStatus;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

public class OfferCalculationFragment extends CLIFragment implements View.OnClickListener, NetworkChangeListener {

	private DeclineOfferInterface declineOfferInterface;
	private static final int INCREASE_PROGRESS_BY = 100;
	private static final int SLIDE_ANIM_DURATION = 1500;
	private HashMap<String, String> mHashIncomeDetail, mHashExpenseDetail;
	private WTextView tvCurrentCreditLimitAmount, tvNewCreditLimitAmount, tvAdditionalCreditLimitAmount, tvCalculatingYourOffer, tvLoadTime, tvSlideToEditSeekInfo, tvSlideToEditAmount;
	private ProgressBar cpCurrentCreditLimit, cpAdditionalCreditLimit, cpNewCreditAmount;
	private LinearLayout llSlideToEditContainer, llNextButtonLayout;
	private FrameLayout flCircularProgressSpinner;
	private WButton btnContinue;
	private SeekBar sbSlideAmount;
	private int mCurrentCredit = 0;
	private OfferActive mObjOffer;
	private CLIPhase2Activity mCliPhase2Activity;
	private ProgressBar mAcceptOfferProgressBar;
	private BroadcastReceiver mConnectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private boolean editorWasShown;
	private WoolworthsApplication woolworthsApplication;
	private boolean mAlreadyLoaded = false;
	private int mCLiId;
	private WGlobalState mGlobalState;
	private RelativeLayout mRelNextButton;
	private FrameLayout flTopLayout;
	private LinearLayout llEmptyLayout;
	private EventStatus mEventStatus;
	private CLIUpdateApplication cliUpdateApplication;
	private CLICreateApplication createOfferTask;
	private za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision cliAcceptOfferDecision;
	private LoadState loadState;
	private final CompositeDisposable disposables = new CompositeDisposable();
	private int mCreditRequestMax;
	public RelativeLayout relConnectionLayout;
	private int currentCredit;

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
			loadState = new LoadState();
			try {
				declineOfferInterface = (DeclineOfferInterface) getActivity();
			} catch (IllegalArgumentException ignored) {
			}
			mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
			woolworthsApplication = ((WoolworthsApplication) getActivity().getApplication());
			mGlobalState = woolworthsApplication.getWGlobalState();
			init(view);
			seekBar();
			Bundle bundle = this.getArguments();
			if (!editorWasShown) {
				onLoad();
				Serializable incomeDetail = null, expenseDetail = null;
				if (bundle != null) {
					incomeDetail = bundle.getSerializable(IncreaseLimitController.INCOME_DETAILS);
					expenseDetail = bundle.getSerializable(IncreaseLimitController.EXPENSE_DETAILS);
				}
				setInvisibleView(tvSlideToEditSeekInfo);
				mCliStepIndicatorListener.onStepSelected(3);
				mObjOffer = ((CLIPhase2Activity) OfferCalculationFragment.this.getActivity()).offerActiveObject();
				mCLiId = mObjOffer.cliId;
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
						cliApplicationRequest(mEventStatus);
					}
				}
			}
			disposables.add(woolworthsApplication
					.bus()
					.toObservable()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Consumer<Object>() {
						@Override
						public void accept(Object object) throws Exception {
							if (object instanceof CLIOfferDecision) {
								cliDelcineOfferRequest(mGlobalState.getDeclineDecision());
							} else if (object instanceof BusStation) {
								final BusStation busStation = (BusStation) object;
								if ((!TextUtils.isEmpty(busStation.getString()) && busStation.getString().equalsIgnoreCase(getString(R.string.decline)))) {
									finishActivity();
								} else if (busStation.getNumber() != null) {
									sbSlideAmount.post(new Runnable() {
										@Override
										public void run() {
											sbSlideAmount.setProgress(busStation.getNumber());
										}
									});
								}
							} else if (object instanceof CustomPopUpWindow) {
								if (mCliPhase2Activity != null)
									mCliPhase2Activity.performClicked();
							}
						}
					}));
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
				int newCLIAmount = mCurrentCredit + progress;
				tvSlideToEditAmount.setText(formatAmount(newCLIAmount));
				String amount = tvSlideToEditAmount.getText().toString();
				tvNewCreditLimitAmount.setText(amount);
				mGlobalState.setCreditLimit(amount);
				tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(calculateAdditionalAmount(currentCredit, tvNewCreditLimitAmount.getText().toString())));
			}
		});
	}

	private void cliCreateApplication(CreateOfferRequest createOfferRequest) {
		onLoad();
		showView(llNextButtonLayout);
		createOfferTask = new CLICreateApplication(getActivity(), createOfferRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				mObjOffer = ((OfferActive) object);
				switch (mObjOffer.httpCode) {
					case 200:
						enableDeclineButton();
						displayApplication(mObjOffer);
						break;

					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
						break;

					default:
						displayMessageError();
						break;
				}
				loadSuccess();
				onLoadComplete();
			}

			@Override
			public void onFailure(final String e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						latestBackgroundTask(LATEST_BACKGROUND_CALL.CREATE_OFFER);
						hideView(llNextButtonLayout);
						loadFailure();
						hideDeclineButton();
						mErrorHandlerView.responseError(view, e);
					}
				});
			}
		});
		createOfferTask.execute();
	}

	private void cliUpdateApplication(CreateOfferRequest createOfferRequest, String cliId) {
		onLoad();
		showView(llNextButtonLayout);
		cliUpdateApplication = new CLIUpdateApplication(getActivity(), createOfferRequest, cliId, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				mObjOffer = ((OfferActive) object);
				switch (mObjOffer.httpCode) {
					case 200:
						enableDeclineButton();
						displayApplication(mObjOffer);
						break;

					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
						break;

					default:
						displayMessageError();
						break;
				}
				onLoadComplete();
				loadSuccess();
			}

			@Override
			public void onFailure(final String e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						latestBackgroundTask(LATEST_BACKGROUND_CALL.UPDATE_APPLICATION);
						loadFailure();
						hideView(llNextButtonLayout);
						hideDeclineButton();
						mErrorHandlerView.responseError(view, e);
					}
				});
			}
		});
		cliUpdateApplication.execute();
	}

	private void cliDelcineOfferRequest(CLIOfferDecision createOfferDecision) {
		declineOfferInterface.onLoad();
		za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision cliOfferDecision = new za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision(getActivity(), createOfferDecision, String.valueOf(mObjOffer.cliId), new OnEventListener() {

			@Override
			public void onSuccess(Object object) {
				OfferActive mObjOffer = ((OfferActive) object);
				switch (mObjOffer.httpCode) {
					case 200:
						finishActivity();
						break;
					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
						break;
					default:
						Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, mObjOffer.response.desc);
						break;
				}

				loadSuccess();
				declineOfferInterface.onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							latestBackgroundTask(LATEST_BACKGROUND_CALL.DECLINE_OFFER);
							loadFailure();
							declineOfferInterface.onLoadComplete();
						}
					});
				}
			}
		});

		cliOfferDecision.execute();
	}

	private void init(View view) {

		mCliPhase2Activity = (CLIPhase2Activity) getActivity();
		mRelNextButton = (RelativeLayout) view.findViewById(R.id.relNextButton);
		tvSlideToEditSeekInfo = (WTextView) view.findViewById(R.id.tvSlideToEditSeekInfo);
		mAcceptOfferProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
		SimpleDraweeView imOfferTime = (SimpleDraweeView) view.findViewById(R.id.imOfferTime);
		relConnectionLayout = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		flTopLayout = (FrameLayout) view.findViewById(R.id.flTopLayout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relConnectionLayout, this);
		mErrorHandlerView.setMargin(relConnectionLayout, 0, 0, 0, 0);
		tvCurrentCreditLimitAmount = (WTextView) view.findViewById(R.id.tvCurrentCreditLimitAmount);
		tvAdditionalCreditLimitAmount = (WTextView) view.findViewById(R.id.tvAdditionalCreditLimitAmount);
		tvNewCreditLimitAmount = (WTextView) view.findViewById(R.id.tvNewCreditLimitAmount);
		tvSlideToEditAmount = (WTextView) view.findViewById(R.id.tvSlideToEditAmount);
		WButton btnRetry = (WButton) view.findViewById(R.id.btnRetry);

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
		btnRetry.setOnClickListener(this);
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
		mCliPhase2Activity.disableDeclineButton();
		hideDeclineButton();
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
		showDeclineButton();
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
		return mNewCreditLimit - currentCreditLimit + INCREASE_PROGRESS_BY;
	}

	private void progressColorFilter(ProgressBar progressBar, int color) {
		progressBar.setIndeterminate(true);
		progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}

	@SuppressLint("CommitTransaction")
	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.btnContinue:
				onAcceptOfferLoad();
				int newCreditLimitAmount = Utils.numericFieldOnly(tvNewCreditLimitAmount.getText().toString());
				CLIOfferDecision createOfferDecision = new CLIOfferDecision(woolworthsApplication.getProductOfferingId(), newCreditLimitAmount, true);
				cliAcceptOfferDecision =
						new za.co.woolworths.financial.services.android.models.rest.CLIOfferDecision(getActivity(), createOfferDecision, String.valueOf(mCLiId), new OnEventListener() {
							@Override
							public void onSuccess(Object object) {
								mObjOffer = ((OfferActive) object);
								switch (mObjOffer.httpCode) {
									case 200:
										String nextStep = mObjOffer.nextStep;
										if (nextStep.toLowerCase().equalsIgnoreCase(getString(R.string.status_poi_required))) {
											Bundle bundle = new Bundle();
											bundle.putString("OFFER_ACTIVE_PAYLOAD", Utils.objectToJson(mObjOffer));
											DocumentFragment documentFragment = new DocumentFragment();
											documentFragment.setArguments(bundle);
											documentFragment.setStepIndicatorListener(mCliStepIndicatorListener);
											FragmentUtils fragmentUtils = new FragmentUtils();
											fragmentUtils.nextFragment((AppCompatActivity) OfferCalculationFragment.this.getActivity(), getFragmentManager().beginTransaction(), documentFragment, R.id.cli_steps_container);
										} else {
											finishActivity();
										}
										break;
									case 440:
										SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
										break;
									default:
										Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, getString(R.string.cli_create_application_error_message));
										break;
								}
								loadSuccess();
								onAcceptOfferCompleted();
							}

							@Override
							public void onFailure(String e) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										latestBackgroundTask(LATEST_BACKGROUND_CALL.ACCEPT_OFFER);
										loadFailure();
										onAcceptOfferCompleted();
									}
								});
							}
						});
				cliAcceptOfferDecision.execute();
				break;

			case R.id.tvSlideToEditAmount:
				try {
					Bundle args = new Bundle();
					String slideAmount = tvSlideToEditAmount.getText().toString();

					args.putInt("slideAmount", Utils.numericFieldOnly(slideAmount));
					args.putInt("currentCredit", mCurrentCredit);
					args.putInt("creditRequestMax", mCreditRequestMax);
					mCliPhase2Activity.actionBarBackIcon();
					EditSlideAmountFragment editAmountFragment = new EditSlideAmountFragment();
					editAmountFragment.setArguments(args);
					editAmountFragment.setStepIndicatorListener(mCliStepIndicatorListener);
					FragmentUtils ftils = new FragmentUtils(false);
					ftils.nextFragment((AppCompatActivity) OfferCalculationFragment.this.getActivity(), getFragmentManager().beginTransaction(), editAmountFragment, R.id.cli_steps_container);

					editorWasShown = true;
				} catch (NullPointerException ignored) {
				}
				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					showView(llNextButtonLayout);
					mErrorHandlerView.hideErrorHandlerLayout();
					cliApplicationRequest(mEventStatus);
				}
				break;
		}
	}

	public CreateOfferRequest createOffer
			(HashMap<String, String> hashIncomeDetail, HashMap<String, String> hashExpenseDetail) {
		return new CreateOfferRequest(
				WoolworthsApplication.getProductOfferingId(),
				Integer.valueOf(hashIncomeDetail.get("GROSS_MONTHLY_INCOME")),
				Integer.valueOf(hashIncomeDetail.get("NET_MONTHLY_INCOME")),
				Integer.valueOf(hashIncomeDetail.get("ADDITIONAL_MONTHLY_INCOME")),
				Integer.valueOf(hashExpenseDetail.get("MORTGAGE_PAYMENTS")),
				Integer.valueOf(hashExpenseDetail.get("RENTAL_PAYMENTS")),
				Integer.valueOf(hashExpenseDetail.get("MAINTENANCE_EXPENSES")),
				Integer.valueOf(hashExpenseDetail.get("MONTHLY_CREDIT_EXPENSES")),
				Integer.valueOf(hashExpenseDetail.get("OTHER_EXPENSES")
				));
	}

	public void displayApplication(OfferActive mObjOffer) {
		if (mObjOffer != null) {
			showDeclineButton();
			switch (mObjOffer.httpCode) {
				case 200:
					enableDeclineButton();
					Offer offer = mObjOffer.offer;
					mCurrentCredit = offer.currCredit + INCREASE_PROGRESS_BY;
					currentCredit = mCurrentCredit;
					mCurrentCredit -= mCurrentCredit % 100;
					String nextStep = mObjOffer.nextStep;
					if (nextStep.toLowerCase().equalsIgnoreCase(getString(R.string.status_offer).toLowerCase())
							|| nextStep.toLowerCase().equalsIgnoreCase(getString(R.string.status_poi_required).toLowerCase())) {
						mCreditRequestMax = offer.creditRequestMax;
						int mDifferenceCreditLimit = (mCreditRequestMax - mCurrentCredit);
						mCLiId = mObjOffer.cliId;
						sbSlideAmount.setMax(mDifferenceCreditLimit);
						sbSlideAmount.incrementProgressBy(INCREASE_PROGRESS_BY);
						animSeekBarToMaximum();
						tvCurrentCreditLimitAmount.setText(formatAmount(currentCredit - INCREASE_PROGRESS_BY));
						tvNewCreditLimitAmount.setText(tvSlideToEditAmount.getText().toString());
						tvAdditionalCreditLimitAmount.setText(additionalAmountSignSum(calculateAdditionalAmount(currentCredit, tvNewCreditLimitAmount.getText().toString())));
						int newCreditLimitAmount = Utils.numericFieldOnly(tvNewCreditLimitAmount.getText().toString());
						mGlobalState.setDecisionDeclineOffer(new CLIOfferDecision(woolworthsApplication.getProductOfferingId(), newCreditLimitAmount, false));
						onLoadComplete();
					} else if (nextStep.toLowerCase().equalsIgnoreCase(getString(R.string.status_decline))) {
						declineMessage();
					} else {
						displayMessageError();
					}
					break;
				case 440:
					SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), mObjOffer.response.stsParams);
					break;

				default:
					displayMessageError();
					break;
			}
		}
	}

	private void displayMessageError() {
		if (mCliPhase2Activity != null)
			mCliPhase2Activity.hideCloseIcon();
		onLoadComplete();
		Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.CLI_ERROR, getString(R.string.cli_create_application_error_message));
	}

	private void declineMessage() {
		if (mCliPhase2Activity != null)
			mCliPhase2Activity.hideCloseIcon();
		onLoadComplete();
		Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.CLI_DECLINE, getString(R.string.cli_declined_popup_title), getString(R.string.cli_declined_popup_description));
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
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mCliPhase2Activity != null) {
			mCliPhase2Activity.actionBarCloseIcon();
		}
		Activity activity = getActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onConnectionChanged() {
		retryConnect();
	}

	private void retryConnect() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (new ConnectionDetector().isOnline(getActivity())) {
						if (!loadState.onLoanCompleted()) {
							if (latest_background_call != null) {
								switch (latest_background_call) {
									case DECLINE_OFFER:
										woolworthsApplication
												.bus()
												.send(new CLIOfferDecision());
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
								mErrorHandlerView.showToast();
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

	public void cliApplicationRequest(EventStatus eventStatus) {
		switch (eventStatus) {
			case CREATE_APPLICATION:
				cliCreateApplication(createOffer(mHashIncomeDetail, mHashExpenseDetail));
				break;

			case UPDATE_APPLICATION:
				cliUpdateApplication(createOffer(mHashIncomeDetail, mHashExpenseDetail), String.valueOf(mCLiId));
				break;
			default:
				displayApplication(mObjOffer);
				break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!disposables.isDisposed())
			disposables.clear();
		cancelRequest(cliUpdateApplication);
		cancelRequest(createOfferTask);
		cancelRequest(cliAcceptOfferDecision);
	}

	public void finishActivity() {
		Activity activity = getActivity();
		if (activity != null) {
			if (woolworthsApplication != null) {
				woolworthsApplication
						.bus()
						.send(new BusStation(true));
			}
			activity.finish();
			activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
		}
	}

	private void loadSuccess() {
		loadState.setLoadComplete(true);
	}

	private void loadFailure() {
		loadState.setLoadComplete(false);
	}

	private void showDeclineButton() {
		mCliPhase2Activity.showDeclineOffer();
	}

	private void hideDeclineButton() {
		mCliPhase2Activity.hideDeclineOffer();
	}

	private void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	private void enableDeclineButton() {
		mCliPhase2Activity.enableDeclineButton();
	}
}