package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.statement.EmailStatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest;
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse;
import za.co.woolworths.financial.services.android.models.dto.statement.USDocuments;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.fragments.statement.EmailStatementFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AppConstant;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.StatementUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper;
import za.co.woolworths.financial.services.android.util.wenum.Delivery;

import static za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.MODAL_LAYOUT.BIOMETRICS_SECURITY_INFO;

public class CustomPopUpWindow extends AppCompatActivity implements View.OnClickListener, NetworkChangeListener {

    public RelativeLayout mRelRootContainer;
    public Animation mPopEnterAnimation;
    public RelativeLayout mRelPopContainer;
    public boolean viewWasClicked = false;
    public static final int ANIM_DOWN_DURATION = 300;
    public WoolworthsApplication woolworthsApplication;
    public WGlobalState mWGlobalState;
    private ProgressBar mWoolworthsProgressBar;
    private WButton mBtnConfirmEmail;
    private WTextView tvAlternativeEmail;
    private StatementUtils mStatementUtils;
    private Call<SendUserStatementResponse> sendUserStatement;
    private BroadcastReceiver mConnectionBroadcast;
    private LoadState loadState;
    private SendUserStatementRequest mSendUserStatementRequest;
    protected TextView mTvStatementSendTo;
    private WButton mNegativeActionButton;
    private WButton mPositiveActionButton;
    private boolean mCloseView;
    public static final int DISMISS_POP_WINDOW_CLICKED = 123400;
    public static int CART_DEFAULT_ERROR_TAPPED = 1234567;

    public enum MODAL_LAYOUT {
        CONFIDENTIAL, INSOLVENCY, INFO, EMAIL, ERROR, MANDATORY_FIELD,
        HIGH_LOAN_AMOUNT, LOW_LOAN_AMOUNT, STORE_LOCATOR_DIRECTION, BARCODE_ERROR,
        SHOPPING_LIST_INFO, INSTORE_AVAILABILITY, NO_STOCK, LOCATION_OFF, SUPPLY_DETAIL_INFO,
        CLI_DANGER_ACTION_MESSAGE_VALIDATION, UPLOAD_DOCUMENT_MODAL, PROOF_OF_INCOME,
        STATEMENT_SENT_TO, CLI_DECLINE, CLI_ERROR, DETERMINE_LOCATION_POPUP, STATEMENT_ERROR, ERROR_TITLE_DESC, SET_UP_BIOMETRICS_ON_DEVICE, BIOMETRICS_SECURITY_INFO}

    MODAL_LAYOUT current_view;
    private String description;
    private String title;
    private String buttonTitle;
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
            buttonTitle = getText(mBundle.getString("buttonTitle"));
            mCloseView = mBundle.getBoolean("closeSlideUpPanel");
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
    protected void onDestroy() {
        super.onDestroy();
        if (sendUserStatement != null && !sendUserStatement.isCanceled()) {
            sendUserStatement.cancel();
        }
    }

    private void displayView(MODAL_LAYOUT current_view) {
        switch (current_view) {
            case BARCODE_ERROR:
                setContentView(R.layout.transparent_activity);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton wButton = findViewById(R.id.btnBarcodeOk);
                wButton.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case SHOPPING_LIST_INFO:
                setContentView(R.layout.shopping_list_info);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                if (description.equalsIgnoreCase("viewShoppingList")) {
                    findViewById(R.id.shoppingListDivider).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnViewShoppingList).setVisibility(View.VISIBLE);
                }
                WButton wButtonOk = findViewById(R.id.btnShopOk);
                WButton wBtnViewShoppingList = findViewById(R.id.btnViewShoppingList);
                wButtonOk.setOnClickListener(this);
                wBtnViewShoppingList.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case BIOMETRICS_SECURITY_INFO:
            case INFO:
                setContentView(R.layout.open_overlay_got_it);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WTextView mOverlayTitle = findViewById(R.id.textApplicationNotProceed);
                TextView mOverlayDescription = findViewById(R.id.overlayDescription);
                WButton mOverlayBtn = findViewById(R.id.btnOverlay);
                LinearLayout mLinEmail = findViewById(R.id.linEmail);
                mLinEmail.setVisibility(View.GONE);
                mOverlayTitle.setVisibility(View.GONE);
                mOverlayDescription.setText(description);
                mOverlayBtn.setText((current_view == BIOMETRICS_SECURITY_INFO) ? getString(R.string.ok) : getString(R.string.got_it));
                mOverlayBtn.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case ERROR:
                setContentView(R.layout.error_popup);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton mBtnOverlay = findViewById(R.id.btnOverlay);
                if (buttonTitle != null && !buttonTitle.isEmpty()) {
                    mBtnOverlay.setText(buttonTitle);
                }
                TextView mDescriptionOverlay = findViewById(R.id.overlayDescription);
                if (description != null)
                    mDescriptionOverlay.setText(description);
                mBtnOverlay.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case CLI_ERROR:
                setContentView(R.layout.error_popup);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnOverlay = findViewById(R.id.btnOverlay);
                TextView descriptionOverlay = findViewById(R.id.overlayDescription);
                if (description != null)
                    descriptionOverlay.setText(description);
                btnOverlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cliExitAnimation();
                    }
                });
                break;

            case STATEMENT_ERROR:
                setContentView(R.layout.statement_error);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnStatement = findViewById(R.id.btnCloseStatement);
                TextView statementOverlay = findViewById(R.id.overlayDescription);
                if (description != null)
                    statementOverlay.setText(description);
                btnStatement.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeStatementAnimation();
                    }
                });
                mRelPopContainer.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                closeStatementAnimation();
                            }
                        });
                break;

            case EMAIL:
                setContentView(R.layout.cli_email_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton mBtnEmailOk = findViewById(R.id.btnEmailOk);
                mBtnEmailOk.setOnClickListener(this);
                WTextView mTextEmailAddress = findViewById(R.id.textEmailAddress);
                if (description != null)
                    mTextEmailAddress.setText(description);
                break;

            case HIGH_LOAN_AMOUNT:
                setContentView(R.layout.error_title_desc_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton mHighLoanAmount = findViewById(R.id.btnLoanHighOk);
                WTextView wTextTitle = findViewById(R.id.title);
                TextView wTextProofIncome = findViewById(R.id.textProofIncome);
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.personalLoanDrawdownAmountHigh, this);
                wTextTitle.setText(getString(R.string.loan_request_high));
//              Fix for all letters are capitalized
                wTextTitle.setAllCaps(false);
                wTextProofIncome.setText(getString(R.string.loan_request_high_desc, description));
                mHighLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case MANDATORY_FIELD:
                setContentView(R.layout.cli_mandatory_error);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnMandatoryOK = findViewById(R.id.btnMandatoryOK);
                WTextView mTextProceed = findViewById(R.id.textApplicationNotProceed);
                mTextProceed.setText(description);
                btnMandatoryOK.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case LOW_LOAN_AMOUNT:
                setContentView(R.layout.error_title_desc_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton mLowLoanAmount = findViewById(R.id.btnLoanHighOk);
                WTextView mTextTitle = findViewById(R.id.title);
                TextView mTextDesc = findViewById(R.id.textProofIncome);
                mTextTitle.setText(getString(R.string.loan_withdrawal_popup_low_error));
//              Fix for all letters are capitalized
                mTextTitle.setAllCaps(false);
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.personalLoanDrawdownAmountLow,this);
                mTextDesc.setText(getString(R.string.loan_request_low_desc));
                if (description != null && TextUtils.isEmpty(description)) {
                    mTextDesc.setText(getString(R.string.loan_request_low_desc).replace
                            ("R1 500.00", CurrencyFormatter.Companion.formatAmountToRandAndCentNoSpace(description)));
                }
                mLowLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case INSOLVENCY:
                setContentView(R.layout.cli_insolvency_popup);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnInsolvencyOK = findViewById(R.id.btnInsolvencyOK);
                btnInsolvencyOK.setOnClickListener(this);
                //mRelPopContainer.setOnClickListener(this);
                break;

            case CONFIDENTIAL:
                setContentView(R.layout.cli_confidential_popup);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnConfidentialOk = findViewById(R.id.btnConfidentialOk);
                btnConfidentialOk.setOnClickListener(this);
                break;

            case INSTORE_AVAILABILITY:
                setContentView(R.layout.instore_availability);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                mLowLoanAmount = findViewById(R.id.btnLoanHighOk);
                mLowLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case NO_STOCK:
                // Firebase event to be triggered when displaying the out of stock dialog
                FirebaseAnalyticsEventHelper.INSTANCE.outOfStock();
                setContentView(R.layout.error_title_desc_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                mLowLoanAmount = findViewById(R.id.btnLoanHighOk);
                mTextTitle = findViewById(R.id.title);
                mTextTitle.setText(getString(R.string.no_stock_title));
                mTextDesc = findViewById(R.id.textProofIncome);
                mTextDesc.setText(getString(R.string.stock_available_product));
                mLowLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case ERROR_TITLE_DESC:
                setContentView(R.layout.error_title_desc_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                mLowLoanAmount = findViewById(R.id.btnLoanHighOk);
                mTextTitle = findViewById(R.id.title);
                mTextDesc = findViewById(R.id.textProofIncome);
                mTextTitle.setText(title);
                mTextDesc.setText(description);
                if (buttonTitle != null && !buttonTitle.isEmpty()) {
                    mLowLoanAmount.setText(buttonTitle);
                }
                mLowLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case LOCATION_OFF:
                setContentView(R.layout.error_title_desc_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                mLowLoanAmount = findViewById(R.id.btnLoanHighOk);
                mTextTitle = findViewById(R.id.title);
                mTextTitle.setText(getString(R.string.location_disable_title));
                mTextDesc = findViewById(R.id.textProofIncome);
                mTextDesc.setText(getString(R.string.location_disable_desc));
                mLowLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case SUPPLY_DETAIL_INFO:
                setContentView(R.layout.supply_detail_info);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                LinearLayout llSupplyDetailContainer = findViewById(R.id.llSupplyDetailContainer);
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
                assert supplyDetailMap != null;
                for (Map.Entry<String, String> entry : supplyDetailMap.entrySet()) {
                    assert inflater != null;
                    @SuppressLint("InflateParams")
                    View view = inflater.inflate(R.layout.supply_detail_row, null, false);
                    LinearLayout.LayoutParams layoutParams =
                            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                    view.setLayoutParams(layoutParams);
                    WTextView tvTitle = view.findViewById(R.id.title);
                    TextView tvDescription = view.findViewById(R.id.description);
                    tvTitle.setText(String.valueOf(entry.getKey()));
                    tvDescription.setText(String.valueOf(entry.getValue()));
                    llSupplyDetailContainer.addView(view);
                }
                mLowLoanAmount = findViewById(R.id.btnLoanHighOk);
                mLowLoanAmount.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;

            case CLI_DANGER_ACTION_MESSAGE_VALIDATION:
                setContentView(R.layout.cli_dangerous_message_validation);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WTextView tvDeclineOffer = findViewById(R.id.tvDeclineOffer);
                TextView tvDeclineOfferDesc = findViewById(R.id.tvDeclineOfferDesc);
                WButton btnCancelDecline = findViewById(R.id.btnCancelDecline);
                WButton btnConfirmDecline = findViewById(R.id.btnConfirmDecline);
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


            case UPLOAD_DOCUMENT_MODAL:
                setContentView(R.layout.document_modal_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnUploadDocuments = findViewById(R.id.btnUploadDocuments);
                mRelPopContainer.setOnClickListener(this);
                btnUploadDocuments.setOnClickListener(this);
                break;

            case PROOF_OF_INCOME:
                setContentView(R.layout.proof_of_income_modal);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton btnOk = findViewById(R.id.btnOk);
                mRelPopContainer.setOnClickListener(this);
                btnOk.setOnClickListener(this);
                break;
            case STATEMENT_SENT_TO:
                setContentView(R.layout.statement_popup_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                mBtnConfirmEmail = findViewById(R.id.btnConfirmEmail);
                tvAlternativeEmail = findViewById(R.id.tvAlternativeEmail);
                mTvStatementSendTo = findViewById(R.id.tvStatementSendTo);
                WTextView tvSendEmail = findViewById(R.id.tvSendEmail);
                mWoolworthsProgressBar = findViewById(R.id.mWoolworthsProgressBar);
                populateDocument(tvSendEmail);
                mSendUserStatementRequest = new Gson().fromJson(userStatement, SendUserStatementRequest.class);
                mSendUserStatementRequest.to = userEmailAddress();
                statementSendToTitle(mTvStatementSendTo, mSendUserStatementRequest.documents);
                tvAlternativeEmail.setOnClickListener(this);
                mBtnConfirmEmail.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;
            case CLI_DECLINE:
                setContentView(R.layout.error_title_desc_layout);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                WButton mCLIDeclineOk = findViewById(R.id.btnLoanHighOk);
                WTextView mCLIDeclineTitle = findViewById(R.id.title);
                TextView mCLIDeclineDesc = findViewById(R.id.textProofIncome);
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

            case DETERMINE_LOCATION_POPUP:
                setContentView(R.layout.determine_popup_location_view);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);

                ImageView imCloseIcon = findViewById(R.id.imCloseIcon);
                Button btnDefaultLocation = findViewById(R.id.btnDefaultLocation);
                Button btnSetNewLocation = findViewById(R.id.btnSetNewLocation);
                btnDefaultLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismissSetMyLocation(ProductState.SET_SUBURB_API);
                    }
                });

                imCloseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startExitAnimation();
                    }
                });

                btnSetNewLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismissSetMyLocation(ProductState.SET_SUBURB);
                    }
                });

                WTextView tvLocation = findViewById(R.id.tvLocation);
                ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
                if (shoppingDeliveryLocation != null) {
                        tvLocation.setText(KotlinUtils.Companion.getPreferredDeliveryType() == Delivery.CNC ? shoppingDeliveryLocation.fulfillmentDetails.getStoreName() : shoppingDeliveryLocation.fulfillmentDetails.getAddress().getAddress1());
                }
                break;

            case SET_UP_BIOMETRICS_ON_DEVICE:
                setContentView(R.layout.sign_out);
                mRelRootContainer = findViewById(R.id.relContainerRootMessage);
                mRelPopContainer = findViewById(R.id.relPopContainer);
                mNegativeActionButton = findViewById(R.id.cancelSignOutButton);
                mPositiveActionButton = findViewById(R.id.btnSignOut);
                WTextView tvTitle = findViewById(R.id.textSignOut);
                TextView tvDescription = findViewById(R.id.overlayDescription);
                mPositiveActionButton.setText(getString(R.string.cli_yes));
                mNegativeActionButton.setText(getString(R.string.cli_no));
                tvTitle.setText(getString(R.string.set_up_device_biometrics_title));
                tvDescription.setText(getString(R.string.set_up_device_biometrics_desc));
                mNegativeActionButton.setOnClickListener(this);
                mPositiveActionButton.setOnClickListener(this);
                mRelPopContainer.setOnClickListener(this);
                break;
            default:
                break;

        }
        setAnimation();
    }


    private void statementSendToTitle(TextView tvStatementSendTo, USDocuments documents) {
        if (documents.document.size() > 1) {
            tvStatementSendTo.setText(getString(R.string.statement_sent_to_title).replace("Statement", "Statements"));
        } else {
            tvStatementSendTo.setText(getString(R.string.statement_sent_to_title));
        }
    }

    public void startExitAnimation() {
        startExitAnimation(Activity.RESULT_CANCELED);
    }

    public void startExitAnimation(final int result) {
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
                    setResult(CART_DEFAULT_ERROR_TAPPED);
                    dismissLayout(result);
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }

    private void cliDeclineAnimation() {
        if (!viewWasClicked) { // prevent more tan one click
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

    private void dismissSetMyLocation(final String setMyLocationType) {
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
                    Utils.sendBus(new ProductState(setMyLocationType));
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
                            .send(new BusStation(getString(R.string.decline)));
                    dismissLayout();
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }

    private void closeStatementAnimation() {
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
                    Utils.sendBus(new StatementFragment());
                    dismissLayout();
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }

    private void finishActivity() {
        SessionUtilities.getInstance().setSTSParameters(null);

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
        dismissLayout(Activity.RESULT_OK);
    }

    private void dismissLayout(int result) {
        Intent returnIntent = new Intent();
        setResult(result, returnIntent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            switch (current_view) {
                default:
                    finishActivity();
                    break;
            }
        }
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
            case R.id.cancelSignOutButton:
            case R.id.btnBarcodeOk:
            case R.id.btnUploadDocuments:
            case R.id.btnShopOk:
            case R.id.btnMandatoryOK:
            case R.id.btnOk:
                if (current_view == BIOMETRICS_SECURITY_INFO) {
                    exitSetupBiometricsAnimation();
                } else {
                    if (v != mRelPopContainer) {
                        whiteEffectClick(mNegativeActionButton);
                    }

                    startExitAnimation();
                }
                break;
            case R.id.relPopContainer:
                if (current_view == BIOMETRICS_SECURITY_INFO) {
                    exitSetupBiometricsAnimation();
                } else {
                    if (v != mRelPopContainer) {
                        whiteEffectClick(mNegativeActionButton);
                    }

                    startExitAnimation(Activity.RESULT_CANCELED);
                }
                break;

            case R.id.btnSignOut:
                if (current_view == MODAL_LAYOUT.SET_UP_BIOMETRICS_ON_DEVICE) {
                    exitSetupBiometricsAnimation();
                }
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

            case R.id.btnConfirmDecline:
                cliDeclineAnimation();
                break;


            case R.id.tvAlternativeEmail:
                exitStatementAnimation();
                break;

            case R.id.btnConfirmEmail:
                sendStatement();
                break;
            case R.id.cancel:
                startExitAnimation();
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
                        Utils.displayValidationMessage(CustomPopUpWindow.this, MODAL_LAYOUT.ERROR, getString(R.string.statement_send_email_false_desc));
                    }
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }

    private void clearHistory() {
        mWGlobalState.setOnBackPressed(false);
        Intent i = new Intent(CustomPopUpWindow.this, BottomNavigationActivity.class);
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

    private void populateDocument(WTextView textView) {
        String email = userEmailAddress();
        textView.setText(email);
    }

    public String userEmailAddress() {
        JWTDecodedModel userDetail = SessionUtilities.getInstance().getJwt();
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
                    Utils.sendBus(new BusStation(userStatement));
                    dismissLayout();
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }

    private void exitAnimationWhenError(String errorMessage) {// close first popup then show error popup
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
                    new Handler().postDelayed(() -> {
                        Utils.displayValidationMessage(CustomPopUpWindow.this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, errorMessage);
                    } ,AppConstant.DELAY_500_MS);
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
        sendUserStatement = new OneAppService().sendStatementRequest(mSendUserStatementRequest);
        sendUserStatement.enqueue(new CompletionHandler<>(new IResponseListener<SendUserStatementResponse>() {
            @Override
            public void onSuccess(SendUserStatementResponse statementResponse) {
                if (statementResponse != null) {
                    Response response = statementResponse.response;
                    switch (statementResponse.httpCode) {
                        case 200:
                            EmailStatementResponse emailResponse;
                            if (!statementResponse.data.isEmpty()) {
                                emailResponse = statementResponse.data.get(0);
                            } else {
                                emailResponse = new EmailStatementResponse();
                                emailResponse.sent = false;
                            }
                            exitStatementConfirmAnimation(emailResponse);
                            break;
                        case 440:
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams, CustomPopUpWindow.this);
                            break;
                        case 403:
                            exitAnimationWhenError(statementResponse.response.desc);
                            break;
                        default:
                            break;
                    }
                }

                loadSuccess();
                onLoadComplete();
            }

            @Override
            public void onFailure(Throwable error) {
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
        }, SendUserStatementResponse.class));
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
                if (NetworkManager.getInstance().isConnectedToNetwork(CustomPopUpWindow.this)) {
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

    private void whiteEffectClick(WButton button) {
        //TODO:: TEST FOR DIFFERENT POPUP
        try {
            if (button != null) {
                button.setBackgroundColor(Color.BLACK);
                button.setTextColor(Color.WHITE);
            }
        } catch (Exception ex) {
            Log.e("whiteEffectClick", ex.toString());
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setResult(resultCode, data);
    }

    private void exitSetupBiometricsAnimation() {
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
                    mWGlobalState.setOnBackPressed(false);
                    setResult(RESULT_OK);
                    dismissLayout();
                }
            });
            mRelRootContainer.startAnimation(animation);
        }
    }
}
