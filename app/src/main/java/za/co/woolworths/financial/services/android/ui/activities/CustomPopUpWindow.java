package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.statement.EmailStatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse;
import za.co.woolworths.financial.services.android.models.rest.SendUserStatement;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.fragments.statement.AlternativeEmailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.EmailStatementFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.JWTHelper;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.StatementUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class CustomPopUpWindow extends AppCompatActivity implements View.OnClickListener, NetworkChangeListener {

	public RelativeLayout mRelRootContainer;
	public Animation mPopEnterAnimation;
	public RelativeLayout mRelPopContainer;
	public boolean viewWasClicked = false;
	public static final int ANIM_DOWN_DURATION = 800;
	public WoolworthsApplication woolworthsApplication;
	public WGlobalState mWGlobalState;
	private ProgressBar mWoolworthsProgressBar;
	private WButton mBtnConfirmEmail;
	private WTextView tvAlternativeEmail;
	private StatementUtils mStatementUtils;
	private SendUserStatement sendUserStatement;
	private BroadcastReceiver mConnectionBroadcast;
	private LoadState loadState;
	private SendUserStatementRequest mSendUserStatementRequest;

	public enum MODAL_LAYOUT {
		CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD,
		HIGH_LOAN_AMOUNT, LOW_LOAN_AMOUNT, STORE_LOCATOR_DIRECTION, SIGN_OUT, BARCODE_ERROR,
		SHOPPING_LIST_INFO, SESSION_EXPIRED, INSTORE_AVAILABILITY, NO_STOCK, LOCATION_OFF, SUPPLY_DETAIL_INFO,
		CLI_DANGER_ACTION_MESSAGE_VALIDATION, SELECT_FROM_DRIVE, AMOUNT_STOCK, UPLOAD_DOCUMENT_MODAL, PROOF_OF_INCOME,
		STATEMENT_SENT_TO, CLI_DECLINE, CLI_ERROR
	}

	MODAL_LAYOUT current_view;
	private String description;
	private String title;
	private String userStatement = "";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, android.R.color.transparent);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		woolworthsApplication = (WoolworthsApplication) CustomPopUpWindow.this.getApplication();
		mWGlobalState = woolworthsApplication.getWGlobalState();
		mStatementUtils = new StatementUtils(CustomPopUpWindow.this);
		Intent intent = getIntent();
		Bundle mBundle = intent.getExtras();
		if (mBundle != null) {
			current_view = (MODAL_LAYOUT) mBundle.getSerializable("key");
			title = getText(mBundle.getString("title"));
			description = getText(mBundle.getString("description"));
			userStatement = mBundle.getString(StatementActivity.SEND_USER_STATEMENT);
			displayView(current_view);
		} else {
			finish();
		}

		mWGlobalState = woolworthsApplication.getWGlobalState();

		loadState = new LoadState();
		loadSuccess();
		mConnectionBroadcast = Utils.connectionBroadCast(this, this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		runningActivityState(true);
	}

	@Override
	protected void onStop() {
		super.onStop();
		runningActivityState(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mStatementUtils != null) {
			mStatementUtils.cancelRequest(sendUserStatement);
		}
		runningActivityState(false);

	}

	private void displayView(MODAL_LAYOUT current_view) {
		switch (current_view) {
			case BARCODE_ERROR:
				setContentView(R.layout.transparent_activity);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton wButton = (WButton) findViewById(R.id.btnBarcodeOk);
				wButton.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case SHOPPING_LIST_INFO:
				setContentView(R.layout.shopping_list_info);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				if (description.equalsIgnoreCase("viewShoppingList")) {
					findViewById(R.id.shoppingListDivider).setVisibility(View.VISIBLE);
					findViewById(R.id.btnViewShoppingList).setVisibility(View.VISIBLE);
				}
				WButton wButtonOk = (WButton) findViewById(R.id.btnShopOk);
				WButton wBtnViewShoppingList = (WButton) findViewById(R.id.btnViewShoppingList);
				wButtonOk.setOnClickListener(this);
				wBtnViewShoppingList.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case SIGN_OUT:
				setContentView(R.layout.sign_out);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mBtnSignOutCancel = (WButton) findViewById(R.id.btnSignOutCancel);
				WButton mBtnSignOut = (WButton) findViewById(R.id.btnSignOut);
				mBtnSignOutCancel.setOnClickListener(this);
				mBtnSignOut.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case INFO:
				setContentView(R.layout.open_overlay_got_it);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WTextView mOverlayTitle = (WTextView) findViewById(R.id.textApplicationNotProceed);
				WTextView mOverlayDescription = (WTextView) findViewById(R.id.overlayDescription);
				WButton mOverlayBtn = (WButton) findViewById(R.id.btnOverlay);
				LinearLayout mLinEmail = (LinearLayout) findViewById(R.id.linEmail);
				mLinEmail.setVisibility(View.GONE);
				mOverlayTitle.setVisibility(View.GONE);
				mOverlayDescription.setText(description);
				mOverlayBtn.setText(getString(R.string.cli_got_it));
				mOverlayBtn.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case ERROR:
				setContentView(R.layout.error_popup);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mBtnOverlay = (WButton) findViewById(R.id.btnOverlay);
				WTextView mDescriptionOverlay = (WTextView) findViewById(R.id.overlayDescription);
				if (description != null)
					mDescriptionOverlay.setText(description);
				mBtnOverlay.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case CLI_ERROR:
				setContentView(R.layout.error_popup);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnOverlay = (WButton) findViewById(R.id.btnOverlay);
				WTextView descriptionOverlay = (WTextView) findViewById(R.id.overlayDescription);
				if (description != null)
					descriptionOverlay.setText(description);
				btnOverlay.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cliExitAnimation();
					}
				});
				mRelPopContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cliExitAnimation();
					}
				});
				break;

			case EMAIL:
				setContentView(R.layout.cli_email_layout);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mBtnEmailOk = (WButton) findViewById(R.id.btnEmailOk);
				mBtnEmailOk.setOnClickListener(this);
				WTextView mTextEmailAddress = (WTextView) findViewById(R.id.textEmailAddress);
				if (description != null)
					mTextEmailAddress.setText(description);
				break;

			case HIGH_LOAN_AMOUNT:
				setContentView(R.layout.lw_too_high_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mHighLoanAmount = (WButton) findViewById(R.id.btnLoanHighOk);
				WTextView wTextTitle = (WTextView) findViewById(R.id.title);
				WTextView wTextProofIncome = (WTextView) findViewById(R.id.textProofIncome);
				wTextTitle.setText(getString(R.string.loan_request_high));
				wTextProofIncome.setText(getString(R.string.loan_request_high_desc));
				mHighLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case MANDATORY_FIELD:
				setContentView(R.layout.cli_mandatory_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnMandatoryOK = (WButton) findViewById(R.id.btnMandatoryOK);
				WTextView mTextProceed = (WTextView) findViewById(R.id.textApplicationNotProceed);
				mTextProceed.setText(description);
				btnMandatoryOK.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case LOW_LOAN_AMOUNT:
				setContentView(R.layout.lw_too_high_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mLowLoanAmount = (WButton) findViewById(R.id.btnLoanHighOk);
				WTextView mTextTitle = (WTextView) findViewById(R.id.title);
				WTextView mTextProofIncome = (WTextView) findViewById(R.id.textProofIncome);
				mTextTitle.setText(getString(R.string.loan_withdrawal_popup_low_error));
				mTextProofIncome.setText(getString(R.string.loan_request_low_desc));
				if (description != null && TextUtils.isEmpty(description)) {
					mTextProofIncome.setText(getString(R.string.loan_request_low_desc).replace
							("R1 500.00", WFormatter.formatAmount(description)));
				}
				mLowLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case INSOLVENCY:
				setContentView(R.layout.cli_insolvency_popup);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnInsolvencyOK = (WButton) findViewById(R.id.btnInsolvencyOK);
				btnInsolvencyOK.setOnClickListener(this);
				//mRelPopContainer.setOnClickListener(this);
				break;

			case CONFIDENTIAL:
				setContentView(R.layout.cli_confidential_popup);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnConfidentialOk = (WButton) findViewById(R.id.btnConfidentialOk);
				btnConfidentialOk.setOnClickListener(this);
				break;

			case SESSION_EXPIRED:
				mWGlobalState.setOnBackPressed(true);
				setContentView(R.layout.session_expired);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WTextView tvSessionExpiredDesc = (WTextView) findViewById(R.id.tvSessionExpiredDesc);
				if (mWGlobalState.fragmentIsReward()) {
					tvSessionExpiredDesc.setText(getString(R.string.session_expired_reward_desc));
				} else {
					mWGlobalState.setAccountSignInState(false);
					tvSessionExpiredDesc.setText(getString(R.string.session_expired_account_desc));
				}
				WButton mBtnSessionExpiredCancel = (WButton) findViewById(R.id.btnSECancel);
				WButton mBtnSignIn = (WButton) findViewById(R.id.btnSESignIn);
				mBtnSessionExpiredCancel.setOnClickListener(this);
				mBtnSignIn.setOnClickListener(this);
				break;

			case INSTORE_AVAILABILITY:
				setContentView(R.layout.instore_availability);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				mLowLoanAmount = (WButton) findViewById(R.id.btnLoanHighOk);
				mLowLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case NO_STOCK:
				setContentView(R.layout.lw_too_high_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				mLowLoanAmount = (WButton) findViewById(R.id.btnLoanHighOk);
				mTextTitle = (WTextView) findViewById(R.id.title);
				mTextTitle.setText(getString(R.string.no_stock_title));
				mTextProofIncome = (WTextView) findViewById(R.id.textProofIncome);
				mTextProofIncome.setText(getString(R.string.stock_available_product));
				mLowLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case LOCATION_OFF:
				setContentView(R.layout.lw_too_high_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				mLowLoanAmount = (WButton) findViewById(R.id.btnLoanHighOk);
				mTextTitle = (WTextView) findViewById(R.id.title);
				mTextTitle.setText(getString(R.string.location_disable_title));
				mTextProofIncome = (WTextView) findViewById(R.id.textProofIncome);
				mTextProofIncome.setText(getString(R.string.location_disable_desc));
				mLowLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case SUPPLY_DETAIL_INFO:
				setContentView(R.layout.supply_detail_info);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				LinearLayout llSupplyDetailContainer = (LinearLayout) findViewById(R.id.llSupplyDetailContainer);
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				// convert JSON string to Map
				LinkedHashMap<String, String> supplyDetailMap = null;
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					supplyDetailMap = objectMapper.readValue(description, new TypeReference<LinkedHashMap<String, String>>() {
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (Map.Entry<String, String> entry : supplyDetailMap.entrySet()) {
					assert inflater != null;
					View view = inflater.inflate(R.layout.supply_detail_row, null, false);
					LinearLayout.LayoutParams layoutParams =
							new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.WRAP_CONTENT);
					view.setLayoutParams(layoutParams);
					WTextView tvTitle = (WTextView) view.findViewById(R.id.title);
					WTextView tvDescription = (WTextView) view.findViewById(R.id.description);
					tvTitle.setText(String.valueOf(entry.getKey()));
					tvDescription.setText(String.valueOf(entry.getValue()));
					llSupplyDetailContainer.addView(view);
				}
				mLowLoanAmount = (WButton) findViewById(R.id.btnLoanHighOk);
				mLowLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case CLI_DANGER_ACTION_MESSAGE_VALIDATION:
				setContentView(R.layout.cli_dangerous_message_validation);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WTextView tvDeclineOffer = (WTextView) findViewById(R.id.tvDeclineOffer);
				WTextView tvDeclineOfferDesc = (WTextView) findViewById(R.id.tvDeclineOfferDesc);
				WButton btnCancelDecline = (WButton) findViewById(R.id.btnCancelDecline);
				WButton btnConfirmDecline = (WButton) findViewById(R.id.btnConfirmDecline);
				btnConfirmDecline.setText(getString(R.string.cli_yes));
				btnCancelDecline.setText(getString(R.string.cli_no));
				tvDeclineOffer.setText(getString(R.string.decline_title));
				String creditLimit = mWGlobalState.getCreditLimit();
				if (!TextUtils.isEmpty(creditLimit)) {
					tvDeclineOfferDesc.setText(getString(R.string.decline_desc).replaceAll("#R", creditLimit));
				}
				btnCancelDecline.setOnClickListener(this);
				btnConfirmDecline.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;


			case AMOUNT_STOCK:
				setContentView(R.layout.lw_too_high_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mBtnOk = (WButton) findViewById(R.id.btnLoanHighOk);
				WTextView mAmountTitle = (WTextView) findViewById(R.id.title);
				WTextView mAmountDesc = (WTextView) findViewById(R.id.textProofIncome);
				mAmountTitle.setText(title);
				mAmountDesc.setText(description);
				mBtnOk.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case UPLOAD_DOCUMENT_MODAL:
				setContentView(R.layout.document_modal_layout);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnUploadDocuments = (WButton) findViewById(R.id.btnUploadDocuments);
				mRelPopContainer.setOnClickListener(this);
				btnUploadDocuments.setOnClickListener(this);
				break;

			case PROOF_OF_INCOME:
				setContentView(R.layout.proof_of_income_modal);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnOk = (WButton) findViewById(R.id.btnOk);
				mRelPopContainer.setOnClickListener(this);
				btnOk.setOnClickListener(this);
				break;
			case STATEMENT_SENT_TO:
				setContentView(R.layout.statement_popup_layout);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				mBtnConfirmEmail = (WButton) findViewById(R.id.btnConfirmEmail);
				tvAlternativeEmail = (WTextView) findViewById(R.id.tvAlternativeEmail);
				WTextView tvSendEmail = (WTextView) findViewById(R.id.tvSendEmail);
				mWoolworthsProgressBar = (ProgressBar) findViewById(R.id.mWoolworthsProgressBar);
				populateDocument(tvSendEmail);

				mSendUserStatementRequest = new Gson().fromJson(userStatement, SendUserStatementRequest.class);
				mSendUserStatementRequest.to = userEmailAddress();

				tvAlternativeEmail.setOnClickListener(this);
				mBtnConfirmEmail.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;
			case CLI_DECLINE:
				setContentView(R.layout.lw_too_high_error);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mCLIDeclineOk = (WButton) findViewById(R.id.btnLoanHighOk);
				WTextView mCLIDeclineTitle = (WTextView) findViewById(R.id.title);
				WTextView mCLIDeclineDesc = (WTextView) findViewById(R.id.textProofIncome);
				if (description != null)
					mCLIDeclineDesc.setText(description);
				if (title != null)
					mCLIDeclineTitle.setText(title);
				mCLIDeclineOk.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cliExitAnimation();
					}
				});
				mRelPopContainer.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cliExitAnimation();
					}
				});
				break;
			default:
				break;
		}
		setAnimation();
	}

	private void startExitAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void cliExitAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					woolworthsApplication
							.bus()
							.send(new CLIOfferDecision());
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void finishActivity() {
		mWGlobalState.setNewSTSParams("");
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (mWGlobalState.getOnBackPressed()) {
						clearHistory();
					} else {
						closeActivity();
					}
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void exitAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					Intent intent = new Intent("logOutReceiver");
					sendBroadcast(intent);
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}


	private void exitEmailAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					Intent intent = new Intent("moveToPageBroadcastReceiver");
					sendBroadcast(intent);
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void insolvencyAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					Intent intent = new Intent("insolvencyBroadcastCheck");
					sendBroadcast(intent);
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void confidentialAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					Intent intent = new Intent("confidentialBroadcastCheck");
					sendBroadcast(intent);
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void dismissLayout() {
		finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onBackPressed() {
		finishActivity();
	}

	private void setAnimation() {
		mPopEnterAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.popup_enter);
		mRelRootContainer.startAnimation(mPopEnterAnimation);
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.btnCancelDecline:
			case R.id.btnLoanHighOk:
			case R.id.btnOverlay:
			case R.id.btnSignOutCancel:
			case R.id.btnBarcodeOk:
			case R.id.relPopContainer:
			case R.id.btnUploadDocuments:
			case R.id.btnShopOk:
			case R.id.btnMandatoryOK:
			case R.id.btnOk:
				startExitAnimation();
				break;

			case R.id.btnViewShoppingList:
				Intent shoppingList = new Intent(this, ShoppingListActivity.class);
				startActivity(shoppingList);
				overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				dismissLayout();
				break;

			case R.id.btnSignOut:
				exitAnimation();
				break;

			case R.id.btnEmailOk:
				exitEmailAnimation();
				break;

			case R.id.btnInsolvencyOK:
				insolvencyAnimation();
				break;

			case R.id.btnConfidentialOk:
				confidentialAnimation();
				break;

			case R.id.btnSECancel:
				exitSessionAnimation();
				break;

			case R.id.btnConfirmDecline:
				cliExitAnimation();
				break;

			case R.id.btnSESignIn:
				mWGlobalState.setPressState(WGlobalState.ON_SIGN_IN);
				String mSTSParams = description;
				if (TextUtils.isEmpty(mSTSParams)) {
					mSTSParams = "";
				} else {
					mSTSParams = Utils.getScope(mSTSParams);
				}
				ScreenManager.presentExpiredTokenSSOSignIn(CustomPopUpWindow.this, mSTSParams);
				overridePendingTransition(0, 0);
				finish();
				break;

			case R.id.tvAlternativeEmail:
				exitStatementAnimation();
				break;

			case R.id.btnConfirmEmail:
				sendStatement();
				break;
		}
	}

	private void exitStatementConfirmAnimation(final EmailStatementResponse response) {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (response.sent) { //navigate to success screen
						woolworthsApplication
								.bus()
								.send(new EmailStatementFragment());
						dismissLayout();
					} else {                    //show popalert error
						dismissLayout();
						Utils.displayValidationMessage(CustomPopUpWindow.this, MODAL_LAYOUT.ERROR, response.error);
					}
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void clearHistory() {
		mWGlobalState.setOnBackPressed(false);
		Intent i = new Intent(CustomPopUpWindow.this, WOneAppBaseActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		startExitAnimation();
	}

	private void closeActivity() {
		finish();
		overridePendingTransition(0, 0);
	}

	private void exitSessionAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mWGlobalState.setNewSTSParams(WGlobalState.EMPTY_FIELD);
					mWGlobalState.setPressState(WGlobalState.ON_CANCEL);
					mWGlobalState.setOnBackPressed(false);
					mWGlobalState.setNewSTSParams("");
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private void runningActivityState(boolean state) {
		if (mWGlobalState != null) {
			mWGlobalState.setDefaultPopupState(state);
		}
	}

	public JWTDecodedModel getJWTDecoded() {
		JWTDecodedModel result = new JWTDecodedModel();
		try {
			SessionDao sessionDao = new SessionDao(this, SessionDao.KEY.USER_TOKEN).get();
			if (sessionDao.value != null && !sessionDao.value.equals("")) {
				result = JWTHelper.decode(sessionDao.value);
			}
		} catch (Exception ignored) {
		}
		return result;
	}

	private void populateDocument(WTextView textView) {
		textView.setText(userEmailAddress());
	}

	public String userEmailAddress() {
		JWTDecodedModel userDetail = getJWTDecoded();
		if (userDetail != null) {
			return userDetail.email.get(0);
		}
		return "";
	}

	private void exitStatementAnimation() {
		if (!viewWasClicked) { // prevent more than one click
			viewWasClicked = true;
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mRelRootContainer.getHeight());
			animation.setFillAfter(true);
			animation.setDuration(ANIM_DOWN_DURATION);
			animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					woolworthsApplication
							.bus()
							.send(new BusStation(userStatement));
					dismissLayout();
				}
			});
			mRelRootContainer.startAnimation(animation);
		}
	}

	private String getText(String text) {
		return TextUtils.isEmpty(text) ? "" : text;
	}

	public void sendStatement() {
		onLoad();
		sendUserStatement = new SendUserStatement(CustomPopUpWindow.this, mSendUserStatementRequest, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				SendUserStatementResponse statementResponse = (SendUserStatementResponse) object;
				if (statementResponse != null) {
					Response response = statementResponse.response;
					switch (statementResponse.httpCode) {
						case 200:
							List<EmailStatementResponse> data = statementResponse.data;
							EmailStatementResponse emailResponse = data.get(0);
							exitStatementConfirmAnimation(emailResponse);
							break;
						case 440:
							SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(CustomPopUpWindow.this, response.stsParams);
							break;
						default:
							break;
					}
				}
				loadSuccess();
				onLoadComplete();
			}

			@Override
			public void onFailure(String e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ErrorHandlerView errorHandlerView = new ErrorHandlerView(CustomPopUpWindow.this);
						errorHandlerView.showToast();
						loadFailure();
						onLoadComplete();
					}
				});
			}
		});

		sendUserStatement.execute();
	}

	public void onLoad() {
		mWoolworthsProgressBar.setVisibility(View.VISIBLE);
		mBtnConfirmEmail.setVisibility(View.GONE);
		tvAlternativeEmail.setEnabled(false);
	}

	public void onLoadComplete() {
		mBtnConfirmEmail.setVisibility(View.VISIBLE);
		mWoolworthsProgressBar.setVisibility(View.GONE);
		tvAlternativeEmail.setEnabled(true);
	}


	private void loadSuccess() {
		loadState.setLoadComplete(true);
	}

	private void loadFailure() {
		loadState.setLoadComplete(false);
	}


	private void retryConnect() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (new ConnectionDetector().isOnline(CustomPopUpWindow.this)) {
					if (!loadState.onLoanCompleted()) {
						sendStatement();
					}
				}
			}
		});
	}

	@Override
	public void onConnectionChanged() {
		retryConnect();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(mConnectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mConnectionBroadcast);
	}
}
