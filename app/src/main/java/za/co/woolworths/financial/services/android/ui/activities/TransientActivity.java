package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class TransientActivity extends AppCompatActivity implements View.OnClickListener {

	private RelativeLayout mRelRootContainer;
	private Animation mPopEnterAnimation;
	private RelativeLayout mRelPopContainer;
	private boolean viewWasClicked = false;
	private static final int ANIM_DOWN_DURATION = 700;
	private WoolworthsApplication woolworthsApplication;
	private WGlobalState mWGlobalState;

	public enum VALIDATION_MESSAGE_LIST {
		CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD,
		HIGH_LOAN_AMOUNT, LOW_LOAN_AMOUNT, STORE_LOCATOR_DIRECTION, SIGN_OUT, BARCODE_ERROR,
		SHOPPING_LIST_INFO, SESSION_EXPIRED
	}

	VALIDATION_MESSAGE_LIST current_view;
	String description;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, android.R.color.transparent);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		woolworthsApplication = (WoolworthsApplication) TransientActivity.this.getApplication();
		mWGlobalState = woolworthsApplication.getWGlobalState();

		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null) {
			current_view = (VALIDATION_MESSAGE_LIST) mBundle.getSerializable("key");
			description = mBundle.getString("description");
			if (TextUtils.isEmpty(description)) { //avoid nullpointerexception
				description = "";
			}
			displayView(current_view);
		} else {
			finish();
		}

		mWGlobalState = woolworthsApplication.getWGlobalState();
	}

	private void displayView(VALIDATION_MESSAGE_LIST current_view) {
		switch (current_view) {
			case BARCODE_ERROR:
				setContentView(R.layout.transparent_activity);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				setAnimation();
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
				setAnimation();
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
				setAnimation();
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
				setAnimation();
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
				setAnimation();
				if (description != null)
					mDescriptionOverlay.setText(description);
				mBtnOverlay.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case EMAIL:
				setContentView(R.layout.cli_email_layout);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton mBtnEmailOk = (WButton) findViewById(R.id.btnEmailOk);
				mBtnEmailOk.setOnClickListener(this);
				WTextView mTextEmailAddress = (WTextView) findViewById(R.id.textEmailAddress);
				setAnimation();
				if (description != null)
					mTextEmailAddress.setText(description);
				//mRelPopContainer.setOnClickListener(this);
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
				setAnimation();
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
				setAnimation();
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
				setAnimation();
				mLowLoanAmount.setOnClickListener(this);
				mRelPopContainer.setOnClickListener(this);
				break;

			case INSOLVENCY:
				setContentView(R.layout.cli_insolvency_popup);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnInsolvencyOK = (WButton) findViewById(R.id.btnInsolvencyOK);
				setAnimation();
				btnInsolvencyOK.setOnClickListener(this);
				//mRelPopContainer.setOnClickListener(this);
				break;

			case CONFIDENTIAL:
				setContentView(R.layout.cli_confidential_popup);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WButton btnConfidentialOk = (WButton) findViewById(R.id.btnConfidentialOk);
				setAnimation();
				btnConfidentialOk.setOnClickListener(this);
				break;

			case SESSION_EXPIRED:
				mWGlobalState.setAccountSignInState(false);
				mWGlobalState.setOnBackPressed(true);
				setContentView(R.layout.session_expired);
				mRelRootContainer = (RelativeLayout) findViewById(R.id.relContainerRootMessage);
				mRelPopContainer = (RelativeLayout) findViewById(R.id.relPopContainer);
				WTextView wTvSessionExpiredTitle = (WTextView) findViewById(R.id.tvSessionExpiredTitle);
				if (!mWGlobalState.getRewardSignInState()) {
					wTvSessionExpiredTitle.setText(getString(R.string.session_expired_reward_desc));
				} else {
					wTvSessionExpiredTitle.setText(getString(R.string.session_expired_account_desc));
				}
				setAnimation();
				WButton mBtnSessionExpiredCancel = (WButton) findViewById(R.id.btnSECancel);
				WButton mBtnSignIn = (WButton) findViewById(R.id.btnSESignIn);
				mBtnSessionExpiredCancel.setOnClickListener(this);
				mBtnSignIn.setOnClickListener(this);
				break;

			default:
				break;
		}
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

	private void finishActivity() {
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
		switch (v.getId()) {
			case R.id.btnLoanHighOk:
			case R.id.btnOverlay:
			case R.id.btnSignOutCancel:
			case R.id.btnBarcodeOk:
			case R.id.relPopContainer:
			case R.id.btnShopOk:
			case R.id.btnMandatoryOK:
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
				mWGlobalState.setPressState(WGlobalState.ON_CANCEL);
				mWGlobalState.setOnBackPressed(false);
				Intent i = new Intent(TransientActivity.this, WOneAppBaseActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
				overridePendingTransition(0, 0);
				finish();
				break;

			case R.id.btnSESignIn:
				mWGlobalState.setPressState(WGlobalState.ON_SIGN_IN);
				String mSTSParams = description;
				if (TextUtils.isEmpty(mSTSParams)) {
					mSTSParams = "";
				} else {
					mSTSParams = Utils.getScope(mSTSParams);
				}
				ScreenManager.presentExpiredTokenSSOSignIn(TransientActivity.this, mSTSParams);
				overridePendingTransition(0, 0);
				finish();
				break;

		}
	}

	private void clearHistory() {
		mWGlobalState.setOnBackPressed(false);
		Intent i = new Intent(TransientActivity.this, WOneAppBaseActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		startExitAnimation();
	}

	public void closeActivity() {
		finish();
		overridePendingTransition(0, 0);
	}
}
