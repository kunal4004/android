package za.co.woolworths.financial.services.android.ui.fragments.account;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.awfs.coordination.R;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IAccountCardDetailsContract;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.contracts.ISetUpDeliveryNowLIstner;
import za.co.woolworths.financial.services.android.models.CreditCardDeliveryCardTypes;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse;
import za.co.woolworths.financial.services.android.models.dto.DebitOrder;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode;
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState;
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardActivationState;
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus;
import za.co.woolworths.financial.services.android.models.dto.account.Products;
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse;
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.DeliveryStatus;
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice;
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.CreditReportTUActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.apply_now.AccountSalesActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl;
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView;
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountCardDetailModelImpl;
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountCardDetailPresenterImpl;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.ContactUsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery.SetUpDeliveryNowDialog;
import za.co.woolworths.financial.services.android.ui.fragments.help.HelpSectionFragment;
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.MyPreferencesFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyOrdersAccountFragment;
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.views.NotificationBadge;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.AccountsErrorHandlerFragment;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SignOutFragment;
import za.co.woolworths.financial.services.android.util.CurrencyFormatter;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FirebaseAnalyticsUserProperty;
import za.co.woolworths.financial.services.android.util.FirebaseManager;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.KotlinUtils;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_ACCOUNT;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_REWARD;
import static za.co.woolworths.financial.services.android.ui.fragments.mypreferences.MyPreferencesFragment.IS_NON_WFS_USER;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_EXPECTATION_FAILED_502;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_OK;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_SESSION_TIMEOUT_400;
import static za.co.woolworths.financial.services.android.util.AppConstant.HTTP_SESSION_TIMEOUT_440;
import static za.co.woolworths.financial.services.android.util.Utils.ACCOUNT_CHARGED_OFF;
import static za.co.woolworths.financial.services.android.util.Utils.hideView;

public class MyAccountsFragment extends Fragment implements OnClickListener, MyAccountsNavigator, WMaterialShowcaseView.IWalkthroughActionListener, IAccountCardDetailsContract.AccountCardDetailView {

    private final String TAG = this.getClass().getSimpleName();

    private RelativeLayout openMessageActivity;
    private RelativeLayout applyCreditCardView;
    private RelativeLayout applyStoreCardView;
    private RelativeLayout applyPersonalCardView;
    private RelativeLayout linkedCreditCardView;
    private RelativeLayout linkedStoreCardView;
    private RelativeLayout linkedPersonalCardView;
    private LinearLayout linkedAccountsLayout;
    private LinearLayout applyNowAccountsLayout;
    private LinearLayout loggedOutHeaderLayout;
    private LinearLayout loggedInHeaderLayout;
    private RelativeLayout unlinkedLayout;
    private RelativeLayout signOutRelativeLayout;
    private RelativeLayout profileRelativeLayout;
    private RelativeLayout preferenceRelativeLayout;

    private WTextView sc_available_funds;
    private WTextView cc_available_funds;
    private WTextView pl_available_funds;
    private WTextView messageCounter;
    private TextView userName;
    private ImageView imgCreditCard;
    private FrameLayout imgStoreCardContainer;
    private FrameLayout imgPersonalLoanCardContainer;
    private static final int ACCOUNT_CARD_REQUEST_CODE = 2043;
    public static final int RESULT_CODE_LINK_DEVICE = 5432;
    public static final int RESULT_CODE_DEVICE_LINKED = 5431;

    private final List<String> unavailableAccounts;
    private AccountsResponse mAccountResponse; //purely referenced to be passed forward as Intent Extra

    private NestedScrollView mScrollView;
    private ErrorHandlerView mErrorHandlerView;
    private LinearLayout allUserOptionsLayout;
    private LinearLayout loginUserOptionsLayout;
    private ImageView imgStoreCardStatusIndicator;
    private ImageView imgCreditCardStatusIndicator;
    private ImageView imgPersonalLoanStatusIndicator;
    private ImageView imgStoreCardApplyNow;
    private int promptsActionListener;
    private boolean isActivityInForeground;
    private boolean isPromptsShown;
    private boolean isAccountsCallMade;
    private RelativeLayout updatePasswordRelativeLayout;
    private UpdateMyAccount mUpdateMyAccount;
    private ImageView imRefreshAccount;
    private RelativeLayout storeLocatorRelativeLayout;
    private RelativeLayout helpSectionRelativeLayout;
    private int httpCode = 0;
    private ImageView messagesRightArrow;
    private ProgressBar pbAccount;
    private FrameLayout imgCreditCardLayout;
    private Call<MessageResponse> messageRequestCall;
    private Account mCreditCardAccount;
    private View linkedAccountBottomDivider;
    private FloatingActionButton chatWithAgentFloatingButton;
    private ImageView creditReportIcon;
    private RelativeLayout creditReportView;
    private HashMap<Products, Account> mAccountsHashMap;


    RelativeLayout contactUs;
    private JsonObject deepLinkParams;
    AccountCardDetailPresenterImpl mCardPresenterImpl = null;
    ISetUpDeliveryNowLIstner mSetUpDeliveryListner = null;
    Bundle mDeepLinkData;

    private TextView retryStoreCardTextView;
    private ImageView retryStoreCardImageView;
    private TextView retryCreditCardTextView;
    private ImageView retryCreditCardImageView;
    private TextView retryPersonalLoanTextView;
    private ImageView retryPersonalLoanImageView;
    private LinearLayout retryStoreCardLinearLayout;
    private LinearLayout retryCreditCardLinearLayout;
    private LinearLayout retryPersonalLoanLinearLayout;
    private ArrayList<UserDevice> deviceList;
    private NotificationBadge notificationBadge;
    private ImageView onlineIndicatorImageView;
    private ChatFloatingActionButtonBubbleView inAppChatTipAcknowledgement;

    public MyAccountsFragment() {
        // Required empty public constructor
        this.unavailableAccounts = new ArrayList<>();
        this.mAccountsHashMap = new HashMap<>();
        this.mAccountResponse = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_accounts_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Trigger Firebase Tag.
        Activity activity = getActivity();
        if (activity == null) return;
        JWTDecodedModel jwtDecodedModel = SessionUtilities.getInstance().getJwt();
        Map<String, String> arguments = new HashMap<>();
        arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, (jwtDecodedModel.C2Id != null) ? jwtDecodedModel.C2Id : "");
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ACCOUNTSEVENTSAPPEARED, arguments, activity);
        setHasOptionsMenu(false);
        mCardPresenterImpl = new AccountCardDetailPresenterImpl(this, new AccountCardDetailModelImpl());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MyAccountActivity) {
            if (getActivity() != null) {
                if (((MyAccountActivity) getActivity()).getSupportActionBar() != null)
                    ((MyAccountActivity) getActivity()).getSupportActionBar().hide();
            }
        }
        if (savedInstanceState == null) {
            hideToolbar();
            setToolbarBackgroundColor(R.color.white);
            openMessageActivity = view.findViewById(R.id.openMessageActivity);
            ImageView openShoppingList = view.findViewById(R.id.openShoppingList);
            contactUs = view.findViewById(R.id.contactUs);
            pbAccount = view.findViewById(R.id.pbAccount);
            applyStoreCardView = view.findViewById(R.id.applyStoreCard);
            applyCreditCardView = view.findViewById(R.id.applyCrediCard);
            applyPersonalCardView = view.findViewById(R.id.applyPersonalLoan);
            linkedCreditCardView = view.findViewById(R.id.linkedCrediCard);
            linkedStoreCardView = view.findViewById(R.id.linkedStoreCard);
            linkedAccountBottomDivider = view.findViewById(R.id.linkedAccountBottomDivider);
            linkedPersonalCardView = view.findViewById(R.id.linkedPersonalLoan);
            linkedAccountsLayout = view.findViewById(R.id.linkedLayout);
            mScrollView = view.findViewById(R.id.nest_scrollview);
            applyNowAccountsLayout = view.findViewById(R.id.applyNowLayout);
            loggedOutHeaderLayout = view.findViewById(R.id.loggedOutHeaderLayout);
            loggedInHeaderLayout = view.findViewById(R.id.loggedInHeaderLayout);
            unlinkedLayout = view.findViewById(R.id.llUnlinkedAccount);
            signOutRelativeLayout = view.findViewById(R.id.signOutBtn);
            profileRelativeLayout = view.findViewById(R.id.rlProfile);
            updatePasswordRelativeLayout = view.findViewById(R.id.rlUpdatePassword);
            preferenceRelativeLayout = view.findViewById(R.id.rlMyPreferences);
            sc_available_funds = view.findViewById(R.id.sc_available_funds);
            cc_available_funds = view.findViewById(R.id.cc_available_funds);
            pl_available_funds = view.findViewById(R.id.pl_available_funds);
            messagesRightArrow = view.findViewById(R.id.messagesRightArrow);
            messageCounter = view.findViewById(R.id.messageCounter);
            userName = view.findViewById(R.id.user_name);
            imgCreditCard = view.findViewById(R.id.imgCreditCard);
            helpSectionRelativeLayout = view.findViewById(R.id.helpSection);
            RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
            mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
            mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
            storeLocatorRelativeLayout = view.findViewById(R.id.storeLocator);
            allUserOptionsLayout = view.findViewById(R.id.parentOptionsLayout);
            loginUserOptionsLayout = view.findViewById(R.id.loginUserOptionsLayout);
            imgStoreCardStatusIndicator = view.findViewById(R.id.storeCardStatusIndicator);
            imgCreditCardStatusIndicator = view.findViewById(R.id.creditCardStatusIndicator);
            imgPersonalLoanStatusIndicator = view.findViewById(R.id.personalLoanStatusIndicator);
            imgStoreCardApplyNow = view.findViewById(R.id.imgStoreCardApply);
            imgStoreCardContainer = view.findViewById(R.id.imgStoreCard);
            imgPersonalLoanCardContainer = view.findViewById(R.id.imgPersonalLoan);
            SwipeRefreshLayout mSwipeToRefreshAccount = view.findViewById(R.id.swipeToRefreshAccount);
            imgCreditCardLayout = view.findViewById(R.id.imgCreditCardLayout);
            RelativeLayout myOrdersRelativeLayout = view.findViewById(R.id.myOrdersRelativeLayout);
            chatWithAgentFloatingButton = view.findViewById(R.id.chatBubbleFloatingButton);
            onlineIndicatorImageView = view.findViewById(R.id.onlineIndicatorImageView);
            notificationBadge = view.findViewById(R.id.badge);
            creditReportView = view.findViewById(R.id.creditReport);
            creditReportIcon = view.findViewById(R.id.creditReportIcon);


            retryStoreCardTextView = view.findViewById(R.id.retryStoreCardTextView);
            retryStoreCardImageView = view.findViewById(R.id.retryStoreCardImageView);

            retryCreditCardTextView = view.findViewById(R.id.retryCreditCardTextView);
            retryCreditCardImageView = view.findViewById(R.id.retryCreditCardImageView);

            retryPersonalLoanImageView = view.findViewById(R.id.retryPersonalLoanImageView);
            retryPersonalLoanTextView = view.findViewById(R.id.retryPersonalLoanTextView);

            retryCreditCardLinearLayout = view.findViewById(R.id.retryCreditCardLinearLayout);
            retryStoreCardLinearLayout = view.findViewById(R.id.retryStoreCardLinearLayout);
            retryPersonalLoanLinearLayout = view.findViewById(R.id.retryPersonalLoanLinearLayout);

            openMessageActivity.setOnClickListener(this);
            contactUs.setOnClickListener(this);
            applyPersonalCardView.setOnClickListener(this);
            applyStoreCardView.setOnClickListener(this);
            applyCreditCardView.setOnClickListener(this);
            linkedStoreCardView.setOnClickListener(this);
            linkedCreditCardView.setOnClickListener(this);
            linkedPersonalCardView.setOnClickListener(this);
            openShoppingList.setOnClickListener(this);
            signOutRelativeLayout.setOnClickListener(this);
            profileRelativeLayout.setOnClickListener(this);
            updatePasswordRelativeLayout.setOnClickListener(this);
            helpSectionRelativeLayout.setOnClickListener(this);
            storeLocatorRelativeLayout.setOnClickListener(this);
            myOrdersRelativeLayout.setOnClickListener(this);
            creditReportView.setOnClickListener(this);

            parseDeepLinkData();

            NavController onBoardingNavigationGraph = Navigation.findNavController(view.findViewById(R.id.on_boarding_navigation_graph));
            KotlinUtils.Companion.setAccountNavigationGraph(onBoardingNavigationGraph, OnBoardingScreenType.ACCOUNT);

            imRefreshAccount = view.findViewById(R.id.imRefreshAccount);
            imRefreshAccount.setOnClickListener(this);

            mUpdateMyAccount = new UpdateMyAccount(mSwipeToRefreshAccount, imRefreshAccount);

            view.findViewById(R.id.loginAccount).setOnClickListener(this.btnSignin_onClick);
            view.findViewById(R.id.registerAccount).setOnClickListener(this.btnRegister_onClick);
            view.findViewById(R.id.llUnlinkedAccount).setOnClickListener(this.btnLinkAccounts_onClick);
            preferenceRelativeLayout.setOnClickListener(this);

            mSwipeToRefreshAccount.setOnRefreshListener(() -> {
                mUpdateMyAccount.setRefreshType(UpdateMyAccount.RefreshAccountType.SWIPE_TO_REFRESH);
                loadAccounts(true);
            });

            view.findViewById(R.id.btnRetry).setOnClickListener(v -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
                    loadAccounts(false);
                }
            });
        }

        // disable refresh icon or pull to refresh if user is not a C2User
        if (!SessionUtilities.getInstance().isC2User()) {
            if (mUpdateMyAccount != null) {
                mUpdateMyAccount.enableSwipeToRefreshAccount(false);
                mUpdateMyAccount.swipeToRefreshAccount(false);
                imRefreshAccount.setEnabled(false);
            } else {
                imRefreshAccount.setEnabled(true);
            }
        }


        if (getActivity() instanceof MyAccountActivity) {
            //hide all views, load accounts may occur
            initialize();
            hideToolbar();
            setToolbarBackgroundColor(R.color.white);
            messageCounterRequest();
        } else {
            callLinkedDevicesAPI(false);
        }

        uniqueIdentifiersForAccount();
    }

    private void callLinkedDevicesAPI(Boolean isForced) {
        Activity activity = getActivity();
        if (activity instanceof BottomNavigationActivity) {
            BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
            Fragment currentFragment = bottomNavigationActivity.getCurrentFragment();
            if ((bottomNavigationActivity.getCurrentSection() == R.id.navigate_to_account)
                    && (currentFragment instanceof MyAccountsFragment)) {
                if (SessionUtilities.getInstance().isUserAuthenticated()) {
                    Call<ViewAllLinkedDeviceResponse> mViewAllLinkedDevices = OneAppService.INSTANCE.getAllLinkedDevices(isForced);
                    mViewAllLinkedDevices.enqueue(new CompletionHandler(new IResponseListener<ViewAllLinkedDeviceResponse>() {
                        @Override
                        public void onFailure(@org.jetbrains.annotations.Nullable Throwable error) {
                            //Do Nothing
                        }

                        @Override
                        public void onSuccess(@org.jetbrains.annotations.Nullable ViewAllLinkedDeviceResponse response) {
                            deviceList = response.getUserDevices();
                        }
                    }, ViewAllLinkedDeviceResponse.class));
                }
            }
        }
    }

    private void validateDeepLinkData() {
        if (deepLinkParams == null || deepLinkParams.get("productGroupCode") == null || !SessionUtilities.getInstance().isUserAuthenticated()) {
            return;
        }
        String productGroupCode = deepLinkParams.get("productGroupCode").getAsString();
        AccountsProductGroupCode groupCode = AccountsProductGroupCode.Companion.getEnum(productGroupCode);
        if (groupCode == null) {
            return;
        }
        switch (groupCode) {
            case STORE_CARD:
                onDeepLinkedProductTap(linkedStoreCardView, applyStoreCardView);
                break;
            case CREDIT_CARD:
                onDeepLinkedProductTap(linkedCreditCardView, applyCreditCardView);
                break;
            case PERSONAL_LOAN:
                onDeepLinkedProductTap(linkedPersonalCardView, applyPersonalCardView);
                break;
        }
        setArguments(null);
        deepLinkParams = null;
    }

    private void parseDeepLinkData() {
        mDeepLinkData = getArguments();
        if (mDeepLinkData == null) {
            return;
        }
        String data = mDeepLinkData.getString("parameters", "").replace("\\", "");
        if (TextUtils.isEmpty(data)) {
            return;
        }
        deepLinkParams = new Gson().fromJson(data, JsonObject.class);

        deepLinkParams.addProperty("feature", mDeepLinkData.getString("feature"));
    }

    private void hideToolbar() {
        Activity activity = getActivity();
        if (activity instanceof BottomNavigationActivity) {
            ((BottomNavigationActivity) activity).hideToolbar();
        }
    }

    private void initialize() {
        this.mAccountResponse = null;
        this.deviceList = new ArrayList(0);
        this.hideAllLayers();
        this.mAccountsHashMap.clear();
        this.unavailableAccounts.clear();
        this.unavailableAccounts.addAll(Arrays.asList(AccountsProductGroupCode.STORE_CARD.getGroupCode(),
                AccountsProductGroupCode.CREDIT_CARD.getGroupCode(), AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode()));
        this.mScrollView.scrollTo(0, 0);
        this.mUpdateMyAccount.setRefreshType(UpdateMyAccount.RefreshAccountType.NONE);
        this.mUpdateMyAccount.swipeToRefreshAccount(false);
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            if (SessionUtilities.getInstance().isC2User()) {
                mUpdateMyAccount.enableSwipeToRefreshAccount(true);
                if (imRefreshAccount != null)
                    imRefreshAccount.setEnabled(true);
                this.loadAccounts(false);
            } else {
                this.configureSignInNoC2ID();
            }
            callLinkedDevicesAPI(false);
        } else {
            if (getActivity() == null) return;
            mUpdateMyAccount.enableSwipeToRefreshAccount(false);
            removeAllBottomNavigationIconBadgeCount();
            configureView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity == null) return;
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.MY_ACCOUNTS);
        isActivityInForeground = true;
        if (!AppInstanceObject.biometricWalkthroughIsPresented(activity))
            messageCounterRequest();

        if (getBottomNavigationActivity() != null && getBottomNavigationActivity().getCurrentFragment() != null
                && getBottomNavigationActivity().getCurrentFragment() instanceof MyAccountsFragment
                && NetworkManager.getInstance().isConnectedToNetwork(activity) && httpCode == HTTP_EXPECTATION_FAILED_502) {
            initialize();
        }
    }

    // add negative sign before currency value
    private String removeNegativeSymbol(SpannableString amount) {
        String currentAmount = amount.toString();
        if (currentAmount.contains("-")) {
            currentAmount = currentAmount.replace("R - ", "- R ");
        }
        return currentAmount;
    }

    private void configureView() {

        this.isAccountsCallMade = true;
        this.unavailableAccounts.clear();
        this.unavailableAccounts.addAll(Arrays.asList(AccountsProductGroupCode.STORE_CARD.getGroupCode(),
                AccountsProductGroupCode.CREDIT_CARD.getGroupCode(), AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode()));
        this.configureAndLayoutTopLayerView();


        //show content for all available products
        for (Map.Entry<Products, Account> item : mAccountsHashMap.entrySet()) {
            Account account = item.getValue();
            Products products = item.getKey();
            switch (AccountsProductGroupCode.Companion.getEnum(products.getProductGroupCode())) {

                case STORE_CARD:
                    showStoreCardContent(account);
                    break;

                case CREDIT_CARD:
                    showCreditCardContent(account);
                    break;

                case PERSONAL_LOAN:
                    showPersonalLoanContent(account);
                    break;
            }
        }

        Map<String, Account> accounts = new HashMap();
        for (Map.Entry<Products, Account> products : mAccountsHashMap.entrySet()) {
            Products product = products.getKey();

            accounts.put(product.getProductGroupCode(), products.getValue());
            int indexOfUnavailableAccount = unavailableAccounts.indexOf(product.getProductGroupCode().toUpperCase());

            if (indexOfUnavailableAccount > -1) {
                try {
                    unavailableAccounts.remove(indexOfUnavailableAccount);
                } catch (Exception e) {
                    FirebaseManager.Companion.logException(e);
                }
            }
        }

        FirebaseAnalyticsUserProperty.Companion.setUserPropertiesForCardProductOfferings(accounts);
        FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCode(accounts);

        //hide content for unavailable products
        boolean sc = true, cc = true, pl = true;
        for (String s : unavailableAccounts) {
            switch (AccountsProductGroupCode.Companion.getEnum(s)) {
                case STORE_CARD:
                    applyStoreCardView.setVisibility(View.VISIBLE);
                    linkedStoreCardView.setVisibility(View.GONE);
                    cc = false;
                    break;
                case CREDIT_CARD:
                    applyCreditCardView.setVisibility(View.VISIBLE);
                    linkedCreditCardView.setVisibility(View.GONE);
                    sc = false;
                    break;
                case PERSONAL_LOAN:
                    applyPersonalCardView.setVisibility(View.VISIBLE);
                    linkedPersonalCardView.setVisibility(View.GONE);
                    pl = false;
                    break;
            }
        }

        if (!sc && !cc && !pl) {
            hideView(linkedAccountsLayout);
        }

        if (unavailableAccounts.size() == 0) {
            //all accounts are shown/linked
            applyNowAccountsLayout.setVisibility(View.GONE);
        } else {
            applyNowAccountsLayout.setVisibility(View.VISIBLE);
        }

        allUserOptionsLayout.setVisibility(View.VISIBLE);
        showFeatureWalkthroughPrompts();
    }

    private void showPersonalLoanContent(Account account) {
        Activity activity = getActivity();
        if (!isAdded() || activity == null || getContext() == null) return;
        showView(linkedPersonalCardView);
        hideView(applyPersonalCardView);
        if (account == null) {
            showView(retryPersonalLoanLinearLayout);
            hideView(pl_available_funds);
            hideView(imgPersonalLoanStatusIndicator);
            linkedAccountsLayout.removeView(linkedPersonalCardView);
            linkedAccountsLayout.addView(linkedPersonalCardView);
            linkedAccountsLayout.removeView(linkedAccountBottomDivider);
            linkedAccountsLayout.addView(linkedAccountBottomDivider);
        } else {
            showView(pl_available_funds);
            hideView(retryPersonalLoanLinearLayout);
            imgPersonalLoanStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
            pl_available_funds.setTextColor(activity.getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
            setAvailableFund(account, pl_available_funds);
        }
    }


    private void setAvailableFund(Account account, WTextView availableFundTextView) {
        if (availableFundTextView == null || account == null) return;
        /**
         * FRS :: https://wigroup2.atlassian.net/wiki/spaces/WAPP/pages/2582478849/FRS+Lite+Remove+Blocks+on+Collections+Customers
         */
        String accountsProductGroupCode = AccountsProductGroupCode.Companion.getEnum(account.productGroupCode).getGroupCode();
        if (accountsProductGroupCode.equalsIgnoreCase(AccountsProductGroupCode.CREDIT_CARD.getGroupCode())
                && account.productOfferingStatus.equalsIgnoreCase(ACCOUNT_CHARGED_OFF)) {
            availableFundTextView.setVisibility(View.GONE);
        } else if (account.productOfferingStatus.equalsIgnoreCase(ACCOUNT_CHARGED_OFF)
                && (accountsProductGroupCode.equalsIgnoreCase(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode()) || accountsProductGroupCode.equalsIgnoreCase(AccountsProductGroupCode.STORE_CARD.getGroupCode()))) {
            availableFundTextView.setVisibility(View.VISIBLE);
            availableFundTextView.setText(FontHyperTextParser.getSpannable(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(account.availableFunds), 1));
        } else {
            availableFundTextView.setVisibility(View.VISIBLE);
            availableFundTextView.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace(account.availableFunds), 1)));
        }
    }

    private void showCreditCardContent(Account account) {
        Activity activity = getActivity();
        if (!isAdded() || activity == null || getContext() == null) return;
        showView(linkedCreditCardView);
        hideView(applyCreditCardView);
        if (account == null) {
            hideView(imgCreditCardStatusIndicator);
            hideView(cc_available_funds);
            showView(retryCreditCardLinearLayout);
            linkedAccountsLayout.removeView(linkedCreditCardView);
            linkedAccountsLayout.addView(linkedCreditCardView);
            linkedAccountsLayout.removeView(linkedAccountBottomDivider);
            linkedAccountsLayout.addView(linkedAccountBottomDivider);
        } else {
            showView(cc_available_funds);
            hideView(retryCreditCardLinearLayout);
            //Check with AccountNumber and change the image accordingly
            this.mCreditCardAccount = account;
            if (account.accountNumberBin != null) {
                if (account.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
                    imgCreditCard.setBackgroundResource(R.drawable.small_5);
                } else if (account.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
                    imgCreditCard.setBackgroundResource(R.drawable.small_4);
                } else if (account.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
                    imgCreditCard.setBackgroundResource(R.drawable.small_3);
                }
            } else {
                imgCreditCard.setBackgroundResource(R.drawable.small_3);
            }

            imgCreditCardStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
            cc_available_funds.setTextColor(activity.getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
            setAvailableFund(account, cc_available_funds);

        }
    }

    private void showStoreCardContent(Account account) {
        Activity activity = getActivity();
        if (!isAdded() || activity == null || getContext() == null) return;
        showView(linkedStoreCardView);
        hideView(applyStoreCardView);
        if (account == null) {
            showView(retryStoreCardLinearLayout);
            hideView(sc_available_funds);
            hideView(imgStoreCardStatusIndicator);
            linkedAccountsLayout.removeView(linkedStoreCardView);
            linkedAccountsLayout.addView(linkedStoreCardView);
            linkedAccountsLayout.removeView(linkedAccountBottomDivider);
            linkedAccountsLayout.addView(linkedAccountBottomDivider);
        } else {
            showView(sc_available_funds);
            hideView(retryStoreCardLinearLayout);
            imgStoreCardStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
            setAvailableFund(account, sc_available_funds);
            sc_available_funds.setTextColor(activity.getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
        }
    }


    private void disableRefresh() {
        if (mUpdateMyAccount != null)
            mUpdateMyAccount.enableSwipeToRefreshAccount(false);
        imRefreshAccount.setVisibility(View.GONE);
        imRefreshAccount.setEnabled(false);
    }

    private void configureSignInNoC2ID() {
        this.configureAndLayoutTopLayerView();

        //show content for all available products
        for (Map.Entry<Products, Account> item : mAccountsHashMap.entrySet()) {
            Products product = item.getKey();
            Account account = item.getValue();
            boolean isAccountValueNullOrEmpty = account == null;
            switch (AccountsProductGroupCode.Companion.getEnum(product.getProductGroupCode())) {
                case STORE_CARD:
                    linkedStoreCardView.setVisibility(View.VISIBLE);
                    applyStoreCardView.setVisibility(View.GONE);
                    if (isAccountValueNullOrEmpty) {
                        imgStoreCardStatusIndicator.setVisibility(View.GONE);
                        sc_available_funds.setVisibility(View.GONE);
                        retryStoreCardLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        sc_available_funds.setVisibility(View.VISIBLE);
                        retryStoreCardLinearLayout.setVisibility(View.GONE);
                        imgStoreCardStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                        setAvailableFund(account, sc_available_funds);
                    }
                    break;
                case CREDIT_CARD:
                    linkedCreditCardView.setVisibility(View.VISIBLE);
                    applyCreditCardView.setVisibility(View.GONE);
                    if (isAccountValueNullOrEmpty) {
                        imgCreditCardStatusIndicator.setVisibility(View.GONE);
                        cc_available_funds.setVisibility(View.GONE);
                        retryCreditCardLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        showView(cc_available_funds);
                        hideView(retryCreditCardLinearLayout);
                        //Check with AccountNumber and change the image accordingly
                        this.mCreditCardAccount = account;
                        if (account.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
                            imgCreditCard.setBackgroundResource(R.drawable.small_5);
                        } else if (account.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
                            imgCreditCard.setBackgroundResource(R.drawable.small_4);
                        } else if (account.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
                            imgCreditCard.setBackgroundResource(R.drawable.small_3);
                        }
                        imgCreditCardStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                        setAvailableFund(account, cc_available_funds);
                    }
                    break;
                case PERSONAL_LOAN:
                    showView(linkedPersonalCardView);
                    hideView(applyPersonalCardView);

                    if (isAccountValueNullOrEmpty) {
                        imgPersonalLoanStatusIndicator.setVisibility(View.GONE);
                        pl_available_funds.setVisibility(View.GONE);
                        retryPersonalLoanLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        pl_available_funds.setVisibility(View.VISIBLE);
                        retryPersonalLoanLinearLayout.setVisibility(View.GONE);
                        imgPersonalLoanStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                        pl_available_funds.setTextColor(getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
                        setAvailableFund(account, pl_available_funds);
                    }
                    break;
            }

        }

        //hide content for unavailable products
        for (String s : unavailableAccounts) {
            switch (AccountsProductGroupCode.Companion.getEnum(s)) {
                case STORE_CARD:
                    showView(applyStoreCardView);
                    hideView(linkedStoreCardView);
                    break;
                case CREDIT_CARD:
                    showView(applyCreditCardView);
                    hideView(linkedCreditCardView);
                    break;
                case PERSONAL_LOAN:
                    showView(applyPersonalCardView);
                    hideView(linkedPersonalCardView);
                    break;
            }
        }

        if (unavailableAccounts.size() == 0) {
            //all accounts are shown/linked
            hideView(applyNowAccountsLayout);
        } else {
            showView(applyNowAccountsLayout);
            disableRefresh();
        }

        showView(allUserOptionsLayout);
        // prompts when user not linked
        isAccountsCallMade = true;
        showFeatureWalkthroughPrompts();
    }

    private void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void configureAndLayoutTopLayerView() {
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            showView(loggedInHeaderLayout);
            //logged in user's name and family name will be displayed on the page
            JWTDecodedModel jwtDecoded = SessionUtilities.getInstance().getJwt();
            String name = jwtDecoded.name.get(0);
            String familyName = jwtDecoded.family_name.get(0);
            userName.setText(String.format("%s %s", name, familyName));
            //initials of the logged in user will be displayed on the page
            showView(signOutRelativeLayout);
            showView(profileRelativeLayout);
            showView(updatePasswordRelativeLayout);
            showView(preferenceRelativeLayout);
            showView(loginUserOptionsLayout);
            if (WoolworthsApplication.getCreditView() != null && WoolworthsApplication.getCreditView().isEnabled())
                showView(creditReportView);
            mUpdateMyAccount.swipeToRefreshAccount(true);
            if (SessionUtilities.getInstance().isC2User())
                showView(linkedAccountsLayout);
            else {
                //user is not linked
                //but signed in
                mUpdateMyAccount.swipeToRefreshAccount(true);
                showView(unlinkedLayout);
            }
        } else {
            //user is signed out
            mUpdateMyAccount.swipeToRefreshAccount(false);
            showView(loggedOutHeaderLayout);
        }
    }

    private void hideAllLayers() {
        hideView(loggedInHeaderLayout);
        hideView(loggedOutHeaderLayout);
        hideView(signOutRelativeLayout);
        hideView(profileRelativeLayout);
        hideView(updatePasswordRelativeLayout);
        hideView(linkedAccountsLayout);
        hideView(applyNowAccountsLayout);
        hideView(allUserOptionsLayout);
        hideView(unlinkedLayout);
        hideView(loginUserOptionsLayout);
        hideView(preferenceRelativeLayout);
        hideView(creditReportView);
    }

    private final OnClickListener btnSignin_onClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity == null || mUpdateMyAccount.accountUpdateActive()) return;
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSIGNIN, activity);
            ScreenManager.presentSSOSignin(getActivity());
        }
    };

    private final OnClickListener btnRegister_onClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity == null || mUpdateMyAccount.accountUpdateActive()) return;
            // disable tap to next view until account response completed
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSREGISTER, activity);
            ScreenManager.presentSSORegister(getActivity());
        }
    };

    private OnClickListener btnLinkAccounts_onClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mUpdateMyAccount.accountUpdateActive()) return;
            ScreenManager.presentSSOLinkAccounts(getActivity());
        }
    };

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        if (activity == null || mUpdateMyAccount.accountUpdateActive()) return;
        switch (v.getId()) {
            case R.id.openMessageActivity:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNT_INBOX, activity);
                Intent openMessageActivity = new Intent(getActivity(), MessagesActivity.class);
                openMessageActivity.putExtra("fromNotification", false);
                startActivity(openMessageActivity);
                getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
                break;
            case R.id.applyStoreCard:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDAPPLYNOW, activity);
                redirectToMyAccountsCardsActivity(ApplyNowState.STORE_CARD);
                break;
            case R.id.applyCrediCard:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDAPPLYNOW, activity);
                if (mCreditCardAccount == null) {
                    redirectToMyAccountsCardsActivity(ApplyNowState.BLACK_CREDIT_CARD);
                    return;
                }
                redirectToMyAccountsCardsActivity(getApplyNowState());
                break;
            case R.id.applyPersonalLoan:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANAPPLYNOW, activity);
                redirectToMyAccountsCardsActivity(ApplyNowState.PERSONAL_LOAN);
                break;
            case R.id.linkedStoreCard:
                navigateToLinkedStoreCard();
                break;
            case R.id.linkedCrediCard:
                navigateToLinkedCreditCard();
                break;
            case R.id.linkedPersonalLoan:
                navigateToLinkedPersonalLoan();
                break;
            case R.id.contactUs:
                if (activity instanceof BottomNavigationActivity) {
                    if (getBottomNavigationActivity() != null)
                        getBottomNavigationActivity().pushFragment(new ContactUsFragment());
                    return;
                }
                if (activity instanceof MyAccountActivity) {
                    ((MyAccountActivity) activity).replaceFragment(new ContactUsFragment());
                }
                break;
            case R.id.helpSection:
                HelpSectionFragment helpSectionFragment = new HelpSectionFragment();
                if (mAccountResponse != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("accounts", Utils.objectToJson(mAccountResponse));
                    helpSectionFragment.setArguments(bundle);
                }
                if (activity instanceof BottomNavigationActivity) {
                    getBottomNavigationActivity().pushFragment(helpSectionFragment);
                    return;
                }

                if (activity instanceof MyAccountActivity) {
                    ((MyAccountActivity) activity).replaceFragment(helpSectionFragment);
                }
                break;
            case R.id.signOutBtn:
                SignOutFragment signOutFragment = new SignOutFragment();
                try {
                    signOutFragment.show((activity instanceof BottomNavigationActivity) ? ((BottomNavigationActivity) activity).getSupportFragmentManager() : ((MyAccountActivity) activity).getSupportFragmentManager(), SignOutFragment.class.getSimpleName());
                } catch (IllegalStateException ex) {
                    FirebaseManager.Companion.logException(ex);
                }
                break;
            case R.id.rlProfile:
                ScreenManager.presentSSOUpdateProfile(activity);
                break;
            case R.id.rlUpdatePassword:
                ScreenManager.presentSSOUpdatePassword(activity);
                break;
            case R.id.storeLocator:
                if (activity instanceof BottomNavigationActivity) {
                    if (getBottomNavigationActivity() != null)
                        getBottomNavigationActivity().pushFragment(new StoresNearbyFragment1());
                    return;
                }

                if (activity instanceof MyAccountActivity) {
                    ((MyAccountActivity) activity).replaceFragment(new StoresNearbyFragment1());
                }
                break;
            case R.id.rlMyPreferences:
                Intent myPreferences = new Intent(getActivity(), MyPreferencesActivity.class);
                myPreferences.putExtra(IS_NON_WFS_USER, unavailableAccounts != null && unavailableAccounts.size() == 3);
                myPreferences.putExtra(MyPreferencesFragment.DEVICE_LIST, deviceList);
                activity.startActivityForResult(myPreferences, RESULT_CODE_DEVICE_LINKED);
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.imRefreshAccount:
                mUpdateMyAccount.setRefreshType(UpdateMyAccount.RefreshAccountType.CLICK_TO_REFRESH);
                loadAccounts(true);
                break;

            case R.id.myOrdersRelativeLayout:
                if (activity instanceof BottomNavigationActivity) {
                    if (getBottomNavigationActivity() != null)
                        getBottomNavigationActivity().pushFragment(new MyOrdersAccountFragment());
                } else {
                    if (activity instanceof MyAccountActivity) {
                        ((MyAccountActivity) activity).replaceFragment(new MyOrdersAccountFragment());
                    }
                }
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Acc_My_Orders, activity);
                break;
            case R.id.creditReport:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Myaccounts_creditview, activity);
                startActivity(new Intent(getActivity(), CreditReportTUActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            default:
                break;

        }
    }

    private void navigateToLinkedStoreCard() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (retryStoreCardLinearLayout.getVisibility() == View.VISIBLE) {
            if (new NetworkManager().isConnectedToNetwork(activity)) {
                Products products = mUpdateMyAccount.getProductByProductGroupCode(AccountsProductGroupCode.STORE_CARD.getGroupCode());
                hideView(retryStoreCardTextView);
                mUpdateMyAccount.retryStoreCardAnimationOnTap(retryStoreCardImageView);
                mUpdateMyAccount.getAccountsByProductGroupCode(products, accountsResponse -> {
                    if (activity instanceof BottomNavigationActivity)
                        showView(retryStoreCardTextView);
                    mUpdateMyAccount.cancelRetryStoreCardAnimation();
                    switch (accountsResponse.httpCode) {
                        case HTTP_OK:
                            if (accountsResponse.accountList != null && accountsResponse.accountList.size() > 0) {
                                Account account = accountsResponse.accountList.get(0);
                                mAccountResponse.accountList.add(account);
                                setAccountResponse(activity, mAccountResponse);

                                hideView(retryStoreCardLinearLayout);
                                showStoreCardContent(account);
                                //set  Firebase user property when retried for specific product
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(AccountsProductGroupCode.STORE_CARD.getGroupCode(), account);
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCodeForProduct(AccountsProductGroupCode.STORE_CARD.getGroupCode(), account);
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesOnRetryPreDelinquencyDebitOrder(AccountsProductGroupCode.STORE_CARD.getGroupCode(), account);

                                if (WoolworthsApplication.getInstance() != null) {

                                    if (!Utils.getLinkDeviceConfirmationShown() && !verifyAppInstanceId() && deepLinkParams == null
                                            && Utils.isGooglePlayServicesAvailable()) {
                                        navigateToLinkDeviceConfirmation(ApplyNowState.STORE_CARD);
                                    } else {
                                        hideView(retryStoreCardLinearLayout);
                                        showStoreCardContent(account);
                                        //set  Firebase user property when retried for specific product
                                        FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCodeForProduct(AccountsProductGroupCode.STORE_CARD.getGroupCode(), account);
                                    }
                                }
                            }
                            break;

                        case HTTP_SESSION_TIMEOUT_440:
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, mAccountResponse.response.stsParams);
                            onSessionExpired(activity);
                            initialize();
                            break;
                        case HTTP_SESSION_TIMEOUT_400:
                            showView(retryStoreCardTextView);
                            mUpdateMyAccount.cancelAnimation();
                            defaultErrorMessage(accountsResponse);
                            break;
                        default:
                            break;
                    }
                    return null;
                }, error -> {
                    activity.runOnUiThread(() -> {
                        showView(retryStoreCardTextView);
                        mUpdateMyAccount.cancelRetryStoreCardAnimation();
                    });
                    return null;
                });
            } else {
                mErrorHandlerView.showToast();
            }
        } else {
            if (!Utils.getLinkDeviceConfirmationShown() && !verifyAppInstanceId() && deepLinkParams == null && Utils.isGooglePlayServicesAvailable()) {
                navigateToLinkDeviceConfirmation(ApplyNowState.STORE_CARD);
            } else {
                redirectToAccountSignInActivity(ApplyNowState.STORE_CARD);
            }
        }
    }

    private boolean verifyAppInstanceId() {
        boolean isLinked = false;
        for (UserDevice device : deviceList) {
            if (Objects.equals(device.getAppInstanceId(), Utils.getUniqueDeviceID(getContext()))) {
                isLinked = true;
                break;
            }
        }
        return isLinked;
    }

    private void navigateToLinkDeviceConfirmation(ApplyNowState applyNowState) {
        Activity activity = getActivity();
        if (activity == null) return;
        Intent intent = new Intent(activity, LinkDeviceConfirmationActivity.class);
        intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, applyNowState);
        if (deepLinkParams != null)
            intent.putExtra(AccountSignedInPresenterImpl.DEEP_LINKING_PARAMS, Utils.objectToJson(deepLinkParams));
        activity.startActivityForResult(intent, RESULT_CODE_LINK_DEVICE);
        activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
        deepLinkParams = null;
    }

    private void navigateToLinkedPersonalLoan() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (retryPersonalLoanLinearLayout.getVisibility() == View.VISIBLE) {
            if (new NetworkManager().isConnectedToNetwork(activity)) {
                Products products = mUpdateMyAccount.getProductByProductGroupCode(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode());
                hideView(retryPersonalLoanTextView);
                mUpdateMyAccount.retryPersonalLoanAnimationOnTap(retryPersonalLoanImageView);
                mUpdateMyAccount.getAccountsByProductGroupCode(products, accountsResponse -> {
                    showView(retryPersonalLoanTextView);
                    mUpdateMyAccount.cancelRetryPersonalLoanAnimation();
                    switch (accountsResponse.httpCode) {
                        case HTTP_OK:
                            if (accountsResponse.accountList != null && accountsResponse.accountList.size() > 0) {
                                Account account = accountsResponse.accountList.get(0);
                                mAccountResponse.accountList.add(account);
                                setAccountResponse(activity, mAccountResponse);
                                hideView(retryPersonalLoanLinearLayout);
                                showPersonalLoanContent(account);
                                //set  Firebase user property when retried for specific product
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode(), account);
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesOnRetryPreDelinquencyDebitOrder(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode(), account);
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCodeForProduct(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode(), account);

                                if (!Utils.getLinkDeviceConfirmationShown() && !verifyAppInstanceId() && deepLinkParams == null
                                        && Utils.isGooglePlayServicesAvailable()) {
                                    navigateToLinkDeviceConfirmation(ApplyNowState.PERSONAL_LOAN);
                                } else {
                                    hideView(retryPersonalLoanLinearLayout);
                                    showPersonalLoanContent(account);
                                    //set  Firebase user property when retried for specific product
                                    FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCodeForProduct(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode(), account);
                                }
                            }
                            break;

                        case HTTP_SESSION_TIMEOUT_440:
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, mAccountResponse.response.stsParams);
                            onSessionExpired(activity);
                            initialize();
                            break;
                        case HTTP_SESSION_TIMEOUT_400:
                            showView(retryPersonalLoanTextView);
                            defaultErrorMessage(accountsResponse);
                            break;
                        default:
                            break;
                    }
                    return null;
                }, error -> {
                    activity.runOnUiThread(() -> {
                        showView(retryPersonalLoanTextView);
                        mUpdateMyAccount.cancelRetryPersonalLoanAnimation();
                    });
                    return null;
                });
            } else {
                mErrorHandlerView.showToast();
            }
        } else {
            if (!Utils.getLinkDeviceConfirmationShown() && !verifyAppInstanceId() && deepLinkParams == null
                    && Utils.isGooglePlayServicesAvailable()) {
                navigateToLinkDeviceConfirmation(ApplyNowState.PERSONAL_LOAN);
            } else {
                redirectToAccountSignInActivity(ApplyNowState.PERSONAL_LOAN);
            }
        }
    }

    private void navigateToLinkedCreditCard() {
        Activity activity = getActivity();
        if (activity == null) return;
        if (retryCreditCardLinearLayout.getVisibility() == View.VISIBLE) {
            if (new NetworkManager().isConnectedToNetwork(activity)) {
                Products products = mUpdateMyAccount.getProductByProductGroupCode(AccountsProductGroupCode.CREDIT_CARD.getGroupCode());
                hideView(retryCreditCardTextView);
                mUpdateMyAccount.retryCreditCardAnimationOnTap(retryCreditCardImageView);
                mUpdateMyAccount.getAccountsByProductGroupCode(products, accountsResponse -> {
                    showView(retryCreditCardTextView);
                    mUpdateMyAccount.cancelRetryCreditCardAnimation();
                    switch (accountsResponse.httpCode) {
                        case HTTP_OK:
                            if (accountsResponse.accountList != null && accountsResponse.accountList.size() > 0) {
                                Account account = accountsResponse.accountList.get(0);
                                mAccountResponse.accountList.add(account);
                                setAccountResponse(activity, mAccountResponse);
                                hideView(retryCreditCardLinearLayout);
                                showCreditCardContent(account);
                                //set  Firebase user property when retried for specific product
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesOnRetryPreDelinquencyPaymentDueDate(AccountsProductGroupCode.CREDIT_CARD.getGroupCode(), account);
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesOnRetryPreDelinquencyDebitOrder(AccountsProductGroupCode.CREDIT_CARD.getGroupCode(), account);
                                FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCodeForProduct(AccountsProductGroupCode.CREDIT_CARD.getGroupCode(), account);

                                if (!Utils.getLinkDeviceConfirmationShown() && !verifyAppInstanceId()&& deepLinkParams == null
                                        && Utils.isGooglePlayServicesAvailable()) {
                                    navigateToLinkDeviceConfirmation(ApplyNowState.SILVER_CREDIT_CARD);
                                } else {
                                    hideView(retryCreditCardLinearLayout);
                                    showCreditCardContent(account);
                                    //set  Firebase user property when retried for specific product
                                    FirebaseAnalyticsUserProperty.Companion.setUserPropertiesDelinquencyCodeForProduct(AccountsProductGroupCode.CREDIT_CARD.getGroupCode(), account);
                                }
                            }
                            break;

                        case HTTP_SESSION_TIMEOUT_440:
                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, mAccountResponse.response.stsParams);
                            onSessionExpired(activity);
                            initialize();
                            break;

                        case HTTP_SESSION_TIMEOUT_400:
                            showView(retryCreditCardTextView);
                            defaultErrorMessage(accountsResponse);
                            break;
                        default:
                            break;
                    }
                    return null;
                }, error -> {
                    activity.runOnUiThread(() -> {
                        showView(retryCreditCardTextView);
                        mUpdateMyAccount.cancelRetryCreditCardAnimation();
                    });
                    return null;
                });
            } else {
                mErrorHandlerView.showToast();
            }
        } else {
            if (!Utils.getLinkDeviceConfirmationShown() && !verifyAppInstanceId() && deepLinkParams == null
                    && Utils.isGooglePlayServicesAvailable()) {
                navigateToLinkDeviceConfirmation(ApplyNowState.SILVER_CREDIT_CARD);
            } else {
                redirectToAccountSignInActivity(ApplyNowState.SILVER_CREDIT_CARD);
            }
        }
    }


    private void defaultErrorMessage(AccountsResponse accountsResponse) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportFragmentManager() != null && accountsResponse != null && accountsResponse.response != null && !TextUtils.isEmpty(accountsResponse.response.desc)) {
            try {
                AccountsErrorHandlerFragment accountsErrorHandlerFragment = AccountsErrorHandlerFragment.Companion.newInstance(accountsResponse.response.desc);
                accountsErrorHandlerFragment.show(activity.getSupportFragmentManager(), AccountsErrorHandlerFragment.class.getSimpleName());
            } catch (IllegalStateException ex) {
                FirebaseManager.Companion.logException(ex);
            }
        }
    }

    private ApplyNowState getApplyNowState() {
        ApplyNowState state = ApplyNowState.BLACK_CREDIT_CARD;
        if (mCreditCardAccount.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
            state = ApplyNowState.SILVER_CREDIT_CARD;
        } else if (mCreditCardAccount.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
            state = ApplyNowState.GOLD_CREDIT_CARD;
        } else if (mCreditCardAccount.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
            state = ApplyNowState.BLACK_CREDIT_CARD;
        }
        return state;
    }

    private void loadAccounts(boolean forceNetworkUpdate) {
        if (!SessionUtilities.getInstance().isC2User()) return;
        mErrorHandlerView.hideErrorHandlerLayout();
        FragmentActivity activity = getActivity();
        if (activity != null)
            mScrollView.setBackgroundColor(ContextCompat.getColor(activity, R.color.recent_search_bg));
        if (forceNetworkUpdate)
            mUpdateMyAccount.swipeToRefreshAccount(true);
        else
            showProgressBar();

        mUpdateMyAccount.make(forceNetworkUpdate, accountsHashMap -> {
            if (activity == null) return null;
            mAccountResponse = mUpdateMyAccount.mAccountResponse;
            switch (mAccountResponse.httpCode) {
                case HTTP_EXPECTATION_FAILED_502:
                case HTTP_OK:
                    mAccountsHashMap = accountsHashMap;
                    FirebaseAnalyticsUserProperty.Companion.setUserPropertiesPreDelinquencyPaymentDueDate(accountsHashMap);
                    FirebaseAnalyticsUserProperty.Companion.setUserPropertiesPreDelinquencyForDebitOrder(accountsHashMap);
                    if (forceNetworkUpdate || ((BottomNavigationActivity) activity).mAccountMasterCache.getAccountsResponse() == null) {
                        setAccountResponse(activity, mAccountResponse);
                    } else {
                        if (activity instanceof BottomNavigationActivity) {
                            mAccountResponse = ((BottomNavigationActivity) activity).mAccountMasterCache.getAccountsResponse();
                            if (isAccountsCallMade)
                                mAccountsHashMap = mUpdateMyAccount.getProductAccountHashMap(mAccountResponse);
                        } else {
                            if (MyAccountActivity.Companion.getMAccountMasterCache() != null && MyAccountActivity.Companion.getMAccountMasterCache().getAccountsResponse() != null) {
                                mAccountResponse = MyAccountActivity.Companion.getMAccountMasterCache().getAccountsResponse();
                                if (isAccountsCallMade)
                                    mAccountsHashMap = mUpdateMyAccount.getProductAccountHashMap(mAccountResponse);
                            }
                        }
                    }

                    if (mAccountResponse == null) {
                        mAccountResponse = mUpdateMyAccount.mAccountResponse;
                    }
                    configureView();

                    validateDeepLinkData();
                    showInAppChat(activity);
                    // # WOP-6284 - Show a retry button on accounts section when an error is returned from server
                    if (activity instanceof BottomNavigationActivity) {
                        if (httpCode == 502 && getBottomNavigationActivity() != null && getBottomNavigationActivity().getCurrentFragment() instanceof MyAccountsFragment) {
                            if (mAccountResponse.response != null && !TextUtils.isEmpty(mAccountResponse.response.desc)) {
                                AccountsErrorHandlerFragment accountsErrorHandlerFragment = AccountsErrorHandlerFragment.Companion.newInstance(mAccountResponse.response.desc);
                                accountsErrorHandlerFragment.show(activity.getSupportFragmentManager(), RootedDeviceInfoFragment.class.getSimpleName());
                            }
                        }
                    }
                    break;

                case HTTP_SESSION_TIMEOUT_440:
                    mUpdateMyAccount.swipeToRefreshAccount(false);
                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, mAccountResponse.response.stsParams);
                    onSessionExpired(activity);
                    initialize();
                    break;

                default:
                    if (mAccountResponse.response != null) {
                        configureView();
                        mUpdateMyAccount.enableSwipeToRefreshAccount(true);
                        mUpdateMyAccount.swipeToRefreshAccount(true);
                        imRefreshAccount.setEnabled(true);
                        Utils.alertErrorMessage(activity, mAccountResponse.response.desc);
                    }
                    break;
            }

            hideProgressBar();
            mUpdateMyAccount.swipeToRefreshAccount(false);
            return null;
        }, error -> {
            if (activity == null) return null;
            activity.runOnUiThread(() -> {
                try {
                    mUpdateMyAccount.swipeToRefreshAccount(false);
                    hideProgressBar();
                } catch (Exception ignored) {
                }
                if (error != null)
                    mErrorHandlerView.networkFailureHandler(error.getMessage());
            });
            return null;
        });
    }

    private void setAccountResponse(Activity activity, AccountsResponse accountsResponse) {
        AccountMasterCache masterCache;
        if (activity instanceof BottomNavigationActivity) {
            masterCache = ((BottomNavigationActivity) activity).mAccountMasterCache;
        } else if (activity instanceof MyAccountActivity)
            masterCache = MyAccountActivity.Companion.getMAccountMasterCache();
        else {
            masterCache = null;
        }
        if (masterCache != null) {
            masterCache.resetAccountResponse();
            masterCache.setAccountsResponse(accountsResponse);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (messageRequestCall != null && !messageRequestCall.isCanceled())
            messageRequestCall.cancel();
        if (getActivity() instanceof BottomNavigationActivity) {
            if (getBottomNavigationActivity() != null & getBottomNavigationActivity().walkThroughPromtView != null) {
                getBottomNavigationActivity().walkThroughPromtView.removeFromWindow();
            }
        }
        if (mUpdateMyAccount != null) {
            this.mUpdateMyAccount.swipeToRefreshAccount(false);
        }

    }

    //	public int getAvailableFundsPercentage(int availableFund, int creditLimit) {
//		// Progressbar MAX value is 10000 to manage float values
//		int percentage = Math.round((100 * ((float) availableFund / (float) creditLimit)) * 100);
//		if (percentage < 0 || percentage > Utils.ACCOUNTS_PROGRESS_BAR_MAX_VALUE)
//			return Utils.ACCOUNTS_PROGRESS_BAR_MAX_VALUE;
//		else
//			return percentage;
//	}


    @SuppressLint("StaticFieldLeak")
    private void onSignOut() {
        try {
            AsyncTask<Void, Void, Void> httpAsyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Activity activity = getActivity();
                        if (activity != null) {
                            Utils.clearCacheHistory(activity);
                        }
                    } catch (Exception pE) {
                        Log.d(TAG, pE.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mScrollView.scrollTo(0, 0);
                }
            };
            httpAsyncTask.execute();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void messageCounterRequest() {
        Activity activity = getActivity();
        if (activity instanceof BottomNavigationActivity) {
            // Enable message counter update if navigator points to account tab only
            BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
            Fragment currentFragment = bottomNavigationActivity.getCurrentFragment();
            if ((bottomNavigationActivity.getCurrentSection() == R.id.navigate_to_account)
                    && (currentFragment instanceof MyAccountsFragment)) {
                if (SessionUtilities.getInstance().isUserAuthenticated()
                        && SessionUtilities.getInstance().isC2User()) {
                    messageRequestCall = OneAppService.INSTANCE.getMessagesResponse(5, 1);
                    messageRequestCall.enqueue(new CompletionHandler<>(new IResponseListener<MessageResponse>() {
                        @Override
                        public void onSuccess(MessageResponse messageResponse) {
                            onMessageResponse(messageResponse.unreadCount);
                        }

                        @Override
                        public void onFailure(Throwable error) {

                        }
                    }, MessageResponse.class));
                }
            }
        }
    }

    @Override
    public void onShoppingListsResponse(ShoppingListsResponse shoppingListsResponse) {
    }

    @Override
    public void onMessageResponse(int unreadCount) {
        Activity activity = getActivity();
        if (activity == null) return;
        Utils.setBadgeCounter(unreadCount);
        addBadge(INDEX_ACCOUNT, unreadCount);
        if (unreadCount > 0) {
            hideView(messagesRightArrow);
            showView(messageCounter);
            messageCounter.setText(String.valueOf(unreadCount));
        } else {
            Utils.removeBadgeCounter();
            hideView(messageCounter);
            showView(messagesRightArrow);
        }
    }

    public void showProgressBar() {
        pbAccount.bringToFront();
        showView(pbAccount);
    }

    public void hideProgressBar() {
        hideView(pbAccount);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //Check if view hierarchy was created
        if (!hidden) {
            //hide all views, load accounts may occur
            initialize();
            hideToolbar();
            setToolbarBackgroundColor(R.color.white);
            messageCounterRequest();

            if (getActivity() instanceof BottomNavigationActivity) {
                //Fixes WOP-3407
                BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) getActivity();
                bottomNavigationActivity.showBottomNavigationMenu();
            }
        } else {
            if (mUpdateMyAccount != null)
                mUpdateMyAccount.swipeToRefreshAccount(false);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Comment what's actually happening here.

        if (requestCode == ScreenManager.BIOMETRICS_LAUNCH_VALUE) {
            if (!isPromptsShown && isAccountsCallMade) {
                isActivityInForeground = true;
                showFeatureWalkthroughPrompts();
            }
        } else if (resultCode == RESULT_CODE_LINK_DEVICE) {
            Serializable intentResult = data.getSerializableExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE);
            if (!(intentResult instanceof ApplyNowState)) {
                return;
            }
            ApplyNowState state = (ApplyNowState) intentResult;
            switch (state) {
                case STORE_CARD:
                    navigateToLinkedStoreCard();
                    break;
                case PERSONAL_LOAN:
                    navigateToLinkedPersonalLoan();
                    break;
                case SILVER_CREDIT_CARD:
                    navigateToLinkedCreditCard();
                    break;
            }
            if (data.getBooleanExtra(MyPreferencesFragment.RESULT_LISTENER_LINK_DEVICE, false)) {
                callLinkedDevicesAPI(true);
            }
        } else if (resultCode == RESULT_CODE_DEVICE_LINKED) {
            callLinkedDevicesAPI(false);
        } else if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            initialize();
            //One time biometricsWalkthrough
            ScreenManager.presentBiometricWalkthrough(getActivity());
        } else if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
            Activity activity = getActivity();
            if (activity == null) return;
            setAccountResponse(activity, null);
            onSignOut();
            initialize();
        } else {
            initialize();
        }
    }

    // To clean up all activities
    private void clearActivityStoryStack() {
        Activity activity = getActivity();
        if (activity == null) return;
        Intent intent = new Intent(activity, BottomNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }

    private void removeAllBottomNavigationIconBadgeCount() {
        addBadge(INDEX_REWARD, 0);
        addBadge(INDEX_ACCOUNT, 0);
        addBadge(INDEX_CART, 0);
    }

    private void addBadge(int section, int count) {
        Activity activity = getActivity();
        if (activity instanceof BottomNavigationActivity) {
            ((BottomNavigationActivity) activity).addBadge(INDEX_ACCOUNT, count);
        }
    }

    private void onSessionExpired(Activity activity) {
        Utils.setBadgeCounter(0);
        removeAllBottomNavigationIconBadgeCount();
        SessionExpiredUtilities.getInstance().showSessionExpireDialog((AppCompatActivity) activity);
        initialize();
    }

    public void scrollToTop() {
        ObjectAnimator anim = ObjectAnimator.ofInt(mScrollView, "scrollY", mScrollView.getScrollY(), 0);
        anim.setDuration(500).start();
    }

    public void showFeatureWalkthroughPrompts() {
        if (isActivityInForeground && SessionUtilities.getInstance().isUserAuthenticated() && getBottomNavigationActivity().getCurrentFragment() instanceof MyAccountsFragment) {
            isPromptsShown = true;
            showFeatureWalkthroughAccounts(unavailableAccounts);
        }
    }

    private void showSetUpDeliveryPopUp() {
        if (mAccountResponse != null && mAccountResponse.accountList != null && mAccountResponse.accountList.size() != 0) {
            Account account = mAccountResponse.accountList.get(0);
            if (account != null && account.cards != null) {
                if (account.cards.get(0).cardStatus != null) {
                    if (account.cards.get(0).cardStatus.equals("PLC") && (account.cards.get(0).envelopeNumber != null)) {
                        List<CreditCardDeliveryCardTypes> cardTypes = WoolworthsApplication.getCreditCardDelivery().getCardTypes();
                        if (cardTypes != null) {
                            for (CreditCardDeliveryCardTypes ccdTypes : cardTypes) {
                                if (ccdTypes.getBinNumber().equalsIgnoreCase(account.accountNumberBin)
                                        && Utils.isFeatureEnabled(ccdTypes.getMinimumSupportedAppBuildNumber())) {
                                    executeCreditCardDeliveryStatusService();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
                loadAccounts(false);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void showFeatureWalkthroughAccounts(List<String> unavailableAccounts) {
        if (getActivity() == null || !AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.account)
            return;
        View viewToScrollUp = null;
        String actionText = getActivity().getResources().getString(R.string.tips_tricks_go_to_accounts);
        if (unavailableAccounts.size() == 3) {
            viewToScrollUp = imgStoreCardApplyNow;
            actionText = getActivity().getResources().getString(R.string.walkthrough_account_action_no_products);
        } else {
            if (!unavailableAccounts.contains(AccountsProductGroupCode.STORE_CARD.getGroupCode())) {
                viewToScrollUp = imgStoreCardContainer;
            } else if (!unavailableAccounts.contains(AccountsProductGroupCode.CREDIT_CARD.getGroupCode())) {
                viewToScrollUp = imgCreditCard;
            } else if (!unavailableAccounts.contains(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode())) {
                viewToScrollUp = imgPersonalLoanCardContainer;
            }
        }
        final View finalTarget1 = viewToScrollUp;
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator.ofInt(mScrollView, "scrollY", finalTarget1.getBottom()).setDuration(300).start();
            }
        });

        promptsActionListener = 1;
        final View target = getTargetView(unavailableAccounts);
        final String finalActionText = actionText;
        final WMaterialShowcaseView.IWalkthroughActionListener listener = this;
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                Activity activity = getActivity();
                if (activity != null || isAdded()) {
                    activity.runOnUiThread(target::invalidate);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Activity activity = getActivity();
                if (activity == null || !isAdded() || getBottomNavigationActivity() == null) return;
                FirebaseManager.Companion.setCrashlyticsString(getString(R.string.crashlytics_materialshowcase_key), this.getClass().getCanonicalName());
                getBottomNavigationActivity().walkThroughPromtView = new WMaterialShowcaseView.Builder(getActivity(), WMaterialShowcaseView.Feature.ACCOUNTS)
                        .setTarget(target)
                        .setTitle(R.string.tips_tricks_view_your_accounts)
                        .setDescription(R.string.tips_tricks_desc_my_accounts)
                        .setActionText(finalActionText)
                        .setImage(R.drawable.tips_tricks_ic_my_accounts)
                        .setAction(listener)
                        .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                        .setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
                getBottomNavigationActivity().walkThroughPromtView.show(activity);
            }
        }.execute();

    }

    private BottomNavigationActivity getBottomNavigationActivity() {
        Activity activity = getActivity();
        if (!(activity instanceof BottomNavigationActivity)) return null;
        return (BottomNavigationActivity) activity;
    }

    @Override
    public void onWalkthroughActionButtonClick(WMaterialShowcaseView.Feature feature) {
        switch (feature) {
            case ACCOUNTS: {
                switch (promptsActionListener) {
                    case 1:
                        if (unavailableAccounts.size() == 3) {
                            onClick(applyStoreCardView);
                        } else {
                            if (!unavailableAccounts.contains(AccountsProductGroupCode.STORE_CARD.getGroupCode())) {
                                onClick(linkedStoreCardView);
                            } else if (!unavailableAccounts.contains(AccountsProductGroupCode.CREDIT_CARD.getGroupCode())) {
                                onClick(linkedCreditCardView);
                            } else if (!unavailableAccounts.contains(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode())) {
                                onClick(linkedPersonalCardView);
                            }
                        }
                        break;
                }
            }
            break;
            case CREDIT_SCORE: {
                onClick(creditReportView);
            }
            break;
            default:
                break;
        }

    }

    @Override
    public void onPromptDismiss() {
        if (isActivityInForeground && SessionUtilities.getInstance().isUserAuthenticated() && getBottomNavigationActivity() != null && getBottomNavigationActivity().getCurrentFragment() instanceof MyAccountsFragment) {
            try {
                showSetUpDeliveryPopUp();
                showInAppChat(getActivity());
            } catch (IndexOutOfBoundsException ex) {
                FirebaseManager.Companion.logException(ex);
            }
        }
    }

    public View getTargetView(List<String> unavailableAccounts) {

        if (unavailableAccounts.size() == 3) {
            return imgStoreCardApplyNow;
        } else {
            if (!unavailableAccounts.contains(AccountsProductGroupCode.STORE_CARD.getGroupCode())) {
                return imgStoreCardContainer;
            } else if (!unavailableAccounts.contains(AccountsProductGroupCode.CREDIT_CARD.getGroupCode())) {
                return imgCreditCardLayout;
            } else if (!unavailableAccounts.contains(AccountsProductGroupCode.PERSONAL_LOAN.getGroupCode())) {
                return imgPersonalLoanCardContainer;
            }
        }
        return imgStoreCardApplyNow;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivityInForeground = false;
    }

    private void uniqueIdentifiersForAccount() {
        Activity activity = getActivity();
        if (activity != null && isAdded()) {
            linkedStoreCardView.setContentDescription(getString(R.string.linked_store_card_layout));
            linkedCreditCardView.setContentDescription(getString(R.string.linked_credit_card_layout));
            linkedPersonalCardView.setContentDescription(getString(R.string.linked_personal_loan_layout));
            applyStoreCardView.setContentDescription(getString(R.string.apply_store_card_layout));
            applyCreditCardView.setContentDescription(getString(R.string.apply_credit_card_layout));
            applyPersonalCardView.setContentDescription(getString(R.string.apply_personal_loan_layout));
            openMessageActivity.setContentDescription(getString(R.string.messages_layout));
            storeLocatorRelativeLayout.setContentDescription(getString(R.string.store_locator_layout));
            helpSectionRelativeLayout.setContentDescription(getString(R.string.need_help_layout));
            profileRelativeLayout.setContentDescription(getString(R.string.profile_layout));
            updatePasswordRelativeLayout.setContentDescription(getString(R.string.update_password_layout));
            preferenceRelativeLayout.setContentDescription(getString(R.string.mypreferences_layout));
            signOutRelativeLayout.setContentDescription(getString(R.string.sign_out_layout));
        }
    }

    private void redirectToAccountSignInActivity(ApplyNowState applyNowState) {
        Activity activity = getActivity();
        if (activity == null) return;
        Intent intent = new Intent(activity, AccountSignedInActivity.class);
        intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, applyNowState);
        intent.putExtra(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE, Utils.objectToJson(mAccountResponse));
        if (deepLinkParams != null)
            intent.putExtra(AccountSignedInPresenterImpl.DEEP_LINKING_PARAMS, Utils.objectToJson(deepLinkParams));
        activity.startActivityForResult(intent, ACCOUNT_CARD_REQUEST_CODE);
        activity.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
    }

    private void redirectToMyAccountsCardsActivity(ApplyNowState applyNowState) {
        Activity activity = getActivity();
        if (activity == null) return;
        Intent intent = new Intent(getActivity(), AccountSalesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("APPLY_NOW_STATE", applyNowState);
        HashMap<String, Account> accountHashMap = new HashMap<>();
        for (Map.Entry<Products, Account> item : mAccountsHashMap.entrySet()) {
            accountHashMap.put(item.getKey().getProductGroupCode(), item.getValue());
        }
        bundle.putString("ACCOUNT_INFO", new Gson().toJson(accountHashMap));
        intent.putExtras(bundle);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
    }

    private void setToolbarBackgroundColor(int color) {
        Activity activity = getActivity();
        if (activity instanceof BottomNavigationActivity) {
            ((BottomNavigationActivity) activity).setToolbarBackgroundColor(color);
        }
    }

    private void showInAppChat(Activity activity) {
        if (!isAdded() || activity == null || mAccountResponse == null || mAccountResponse.accountList == null)
            return;
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.account) {
            AppCompatActivity act;
            if (activity instanceof BottomNavigationActivity)
                act = (BottomNavigationActivity) activity;
            else
                act = (MyAccountActivity) activity;

            inAppChatTipAcknowledgement = new ChatFloatingActionButtonBubbleView(act, new ChatBubbleVisibility(mAccountResponse.accountList, activity), chatWithAgentFloatingButton, ApplyNowState.STORE_CARD, mScrollView,notificationBadge,onlineIndicatorImageView);
            inAppChatTipAcknowledgement.build();
        }
    }

    private void redirectToCreditCardActivity(CreditCardDeliveryStatusResponse creditCardDeliveryStatusResponse, ApplyNowState applyNowState) {
        Account account = mAccountResponse.accountList.get(0);
        Intent intent = new Intent(getContext(), CreditCardDeliveryActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("envelopeNumber", account.cards.get(0).envelopeNumber);
        mBundle.putString("accountBinNumber", account.accountNumberBin);
        mBundle.putString("StatusResponse", Utils.toJson(creditCardDeliveryStatusResponse.getStatusResponse()));
        mBundle.putString("productOfferingId", String.valueOf(account.productOfferingId));
        mBundle.putBoolean("setUpDeliveryNowClicked", true);
        if (applyNowState != null)
            mBundle.putSerializable(AccountSignedInPresenterImpl.APPLY_NOW_STATE, applyNowState);
        intent.putExtra("bundle", mBundle);
        startActivity(intent);
    }

    @Override
    public void handleUnknownHttpCode(@org.jetbrains.annotations.Nullable String description) {

    }

    @Override
    public void handleSessionTimeOut(@org.jetbrains.annotations.Nullable String stsParams) {

    }

    @Override
    public void showStoreCardProgress() {

    }

    @Override
    public void hideStoreCardProgress() {

    }

    @Override
    public void navigateToGetTemporaryStoreCardPopupActivity(@NotNull StoreCardsResponse storeCardResponse) {

    }

    @Override
    public void navigateToDebitOrderActivity(@NotNull DebitOrder debitOrder) {

    }

    @Override
    public void navigateToBalanceProtectionInsurance(@org.jetbrains.annotations.Nullable String accountInfo) {

    }

    @Override
    public void setBalanceProtectionInsuranceState(boolean coveredText) {

    }

    @Override
    public void displayCardHolderName(@org.jetbrains.annotations.Nullable String name) {

    }

    @Override
    public void hideUserOfferActiveProgress() {

    }

    @Override
    public void showUserOfferActiveProgress() {

    }

    @Override
    public void disableContentStatusUI() {

    }

    @Override
    public void enableContentStatusUI() {

    }

    @Override
    public void handleCreditLimitIncreaseTagStatus(@NotNull OfferActive offerActive) {

    }

    @Override
    public void hideProductNotInGoodStanding() {

    }

    @Override
    public void onOfferActiveSuccessResult() {

    }

    @Override
    public void navigateToLoanWithdrawalActivity() {

    }

    @Override
    public void navigateToPaymentOptionActivity() {

    }

    @Override
    public void navigateToPayMyAccountActivity() {

    }

    @Override
    public void onGetCreditCardTokenFailure() {
    }

    @Override
    public void showGetCreditCardActivationStatus(@NotNull CreditCardActivationState status) {

    }

    @Override
    public void executeCreditCardTokenService() {

    }

    @Override
    public void stopCardActivationShimmer() {

    }

    @Override
    public void executeCreditCardDeliveryStatusService() {
        mCardPresenterImpl.getCreditCardDeliveryStatus(mAccountResponse.accountList.get(0).cards.get(0).envelopeNumber, String.valueOf(mAccountResponse.accountList.get(0).productOfferingId));
    }

    @Override
    public void onGetCreditCardDeliveryStatusSuccess(@NotNull CreditCardDeliveryStatusResponse creditCardDeliveryStatusResponse) {
        if (creditCardDeliveryStatusResponse.getStatusResponse().getDeliveryStatus().getStatusDescription().equalsIgnoreCase(CreditCardDeliveryStatus.CARD_RECEIVED.name())) {
            ApplyNowState applyNowState;
            String accountNumberBin = mCreditCardAccount.accountNumberBin;
            if (accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
                applyNowState = ApplyNowState.GOLD_CREDIT_CARD;
            } else if (accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
                applyNowState = ApplyNowState.BLACK_CREDIT_CARD;
            } else if (accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
                applyNowState = ApplyNowState.SILVER_CREDIT_CARD;
            } else {
                applyNowState = null;
            }
            mSetUpDeliveryListner = (ApplyNowState) -> redirectToCreditCardActivity(creditCardDeliveryStatusResponse, applyNowState);
            Bundle bundle = new Bundle();
            bundle.putString("accountBinNumber", accountNumberBin);
            SetUpDeliveryNowDialog setUpDeliveryNowDialog = new SetUpDeliveryNowDialog(bundle, mSetUpDeliveryListner);
            Activity activity = getActivity();
            if (activity == null)
                return;
            setUpDeliveryNowDialog.show(getActivity()
                    .getSupportFragmentManager(), SetUpDeliveryNowDialog.class.getSimpleName());
        }
    }

    @Override
    public void onGetCreditCardDeliveryStatusFailure() {
    }

    @Override
    public void showGetCreditCardDeliveryStatus(@NotNull DeliveryStatus deliveryStatus) {
    }

    @Override
    public void showOnStoreCardFailure(@org.jetbrains.annotations.Nullable Throwable error) {

    }

    @Override
    public void handleStoreCardCardsSuccess(@NotNull StoreCardsResponse storeCardResponse) {

    }

    @Override
    public void showUnBlockStoreCardCardDialog() {

    }

    @Override
    public void navigateToMyCardDetailActivity(@NotNull StoreCardsResponse storeCardResponse, boolean requestUnblockStoreCardCall) {

    }

    @Override
    public void onGetCreditCArdTokenSuccess(@NotNull CreditCardTokenResponse creditCardTokenResponse) {

    }

    private void onDeepLinkedProductTap(RelativeLayout linkedRelativeLayout, RelativeLayout applyNowRelativeLayout) {
        if (!isAdded() || linkedRelativeLayout == null || applyNowRelativeLayout == null) return;
        if (linkedRelativeLayout.getVisibility() == View.VISIBLE)
            linkedRelativeLayout.performClick();
        else
            applyNowRelativeLayout.performClick();
    }

}