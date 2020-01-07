package za.co.woolworths.financial.services.android.ui.fragments.account;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.MyAccountsFragmentBinding;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.account.AccountSalesActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.main_list.ContactUsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.help.HelpSectionFragment;
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_ACCOUNT;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_REWARD;

public class MyAccountsFragment extends BaseFragment<MyAccountsFragmentBinding, MyAccountsViewModel> implements View.OnClickListener, ViewPager.OnPageChangeListener, MyAccountsNavigator, WMaterialShowcaseView.IWalkthroughActionListener {

	private final String TAG = this.getClass().getSimpleName();

	private MyAccountsViewModel myAccountsViewModel;

	RelativeLayout openMessageActivity;
	ImageView openShoppingList;
	RelativeLayout contactUs;
	RelativeLayout applyCreditCardView;
	RelativeLayout applyStoreCardView;
	RelativeLayout applyPersonalCardView;
	RelativeLayout linkedCreditCardView;
	RelativeLayout linkedStoreCardView;
	RelativeLayout linkedPersonalCardView;
	LinearLayout linkedAccountsLayout;
	LinearLayout applyNowAccountsLayout;
	LinearLayout loggedOutHeaderLayout;
	LinearLayout loggedInHeaderLayout;
	RelativeLayout unlinkedLayout;
	RelativeLayout signOutRelativeLayout;
	RelativeLayout profileRelativeLayout;
	RelativeLayout preferenceRelativeLayout;
	ViewPager viewPager;
	MyAccountOverViewPagerAdapter adapter;
	LinearLayout pager_indicator;

	WTextView sc_available_funds;
	WTextView cc_available_funds;
	WTextView pl_available_funds;
	WTextView messageCounter;
	TextView userName;
	private ImageView imgCreditCard;
	private FrameLayout imgStoreCardContainer;
	private FrameLayout imgPersonalLoanCardContainer;

	Map<String, Account> accounts;
	List<String> unavailableAccounts;
	private AccountsResponse mAccountResponse; //purely referenced to be passed forward as Intent Extra

	private int dotsCount;
	private ImageView[] dots;
	private NestedScrollView mScrollView;
	private ErrorHandlerView mErrorHandlerView;
	private LinearLayout allUserOptionsLayout;
	private LinearLayout loginUserOptionsLayout;
	ImageView imgStoreCardStatusIndicator;
	ImageView imgCreditCardStatusIndicator;
	ImageView imgPersonalLoanStatusIndicator;
	ImageView imgStoreCardApplyNow;
	int promptsActionListener;
	boolean isActivityInForeground;
	boolean isPromptsShown;
	boolean isAccountsCallMade;
    private RelativeLayout updatePasswordRelativeLayout;
	private UpdateMyAccount mUpdateMyAccount;
	private ImageView imRefreshAccount;
	private RelativeLayout storeLocatorRelativeLayout;
	private RelativeLayout helpSectionRelativeLayout;

	public MyAccountsFragment() {
		// Required empty public constructor
		this.accounts = new HashMap<>();
		this.unavailableAccounts = new ArrayList<>();
		this.mAccountResponse = null;
	}

	WoolworthsApplication woolworthsApplication;

	@Override
	public MyAccountsViewModel getViewModel() {
		return myAccountsViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.my_accounts_fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Trigger Firebase Tag.

		JWTDecodedModel jwtDecodedModel = SessionUtilities.getInstance().getJwt();
		Map<String, String> arguments = new HashMap<>();
		arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, (jwtDecodedModel.C2Id != null) ? jwtDecodedModel.C2Id : "");
		Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.ACCOUNTSEVENTSAPPEARED, arguments);
		myAccountsViewModel = ViewModelProviders.of(this).get(MyAccountsViewModel.class);
		myAccountsViewModel.setNavigator(this);
		setHasOptionsMenu(false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null) {
			hideToolbar();
			setToolbarBackgroundColor(R.color.white);
			woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
			openMessageActivity = view.findViewById(R.id.openMessageActivity);
			openShoppingList = view.findViewById(R.id.openShoppingList);
			contactUs = view.findViewById(R.id.contactUs);
			applyStoreCardView = view.findViewById(R.id.applyStoreCard);
			applyCreditCardView = view.findViewById(R.id.applyCrediCard);
			applyPersonalCardView = view.findViewById(R.id.applyPersonalLoan);
			linkedCreditCardView = view.findViewById(R.id.linkedCrediCard);
			linkedStoreCardView = view.findViewById(R.id.linkedStoreCard);
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
			viewPager = view.findViewById(R.id.pager);
			pager_indicator = view.findViewById(R.id.viewPagerCountDots);
			sc_available_funds = view.findViewById(R.id.sc_available_funds);
			cc_available_funds = view.findViewById(R.id.cc_available_funds);
			pl_available_funds = view.findViewById(R.id.pl_available_funds);
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
			adapter = new MyAccountOverViewPagerAdapter(getActivity());
			viewPager.addOnPageChangeListener(this);
			setUiPageViewController();

			imRefreshAccount = view.findViewById(R.id.imRefreshAccount);
			imRefreshAccount.setOnClickListener(this);

			mUpdateMyAccount = new UpdateMyAccount(mSwipeToRefreshAccount,imRefreshAccount);

			view.findViewById(R.id.loginAccount).setOnClickListener(this.btnSignin_onClick);
			view.findViewById(R.id.registerAccount).setOnClickListener(this.btnRegister_onClick);
			view.findViewById(R.id.llUnlinkedAccount).setOnClickListener(this.btnLinkAccounts_onClick);
			preferenceRelativeLayout.setOnClickListener(this);

			mSwipeToRefreshAccount.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					mUpdateMyAccount.setRefreshType(UpdateMyAccount.RefreshAccountType.SWIPE_TO_REFRESH);
					loadAccounts(true);
				}
			});

			view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
						loadAccounts(false);
					}
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

		uniqueIdentifiersForAccount();
	}

	private void initialize() {
		this.mAccountResponse = null;
		this.hideAllLayers();
		this.accounts.clear();
		this.unavailableAccounts.clear();
		this.unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));
		this.mScrollView.scrollTo(0, 0);
		this.mUpdateMyAccount.setRefreshType(UpdateMyAccount.RefreshAccountType.NONE);
		this.mUpdateMyAccount.swipeToRefreshAccount(false);
		if (SessionUtilities.getInstance().isUserAuthenticated()) {
			if (SessionUtilities.getInstance().isC2User()) {
				mUpdateMyAccount.enableSwipeToRefreshAccount(true);
				if (imRefreshAccount != null)
					imRefreshAccount.setEnabled(true);
				this.loadAccounts(false);
			}else {
				this.configureSignInNoC2ID();
			}
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
	}

	// add negative sign before currency value
	public String removeNegativeSymbol(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("R - ","- R ");
		}
		return currentAmount;
	}

	private void configureView() {
		this.configureAndLayoutTopLayerView();

		//show content for all available products
		for (Map.Entry<String, Account> item : accounts.entrySet()) {
			Account account = item.getValue();
			switch (account.productGroupCode) {
				case "SC":
					linkedStoreCardView.setVisibility(View.VISIBLE);
					applyStoreCardView.setVisibility(View.GONE);
					imgStoreCardStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
					sc_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
                    sc_available_funds.setTextColor(getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
                    break;
				case "CC":
					linkedCreditCardView.setVisibility(View.VISIBLE);
					applyCreditCardView.setVisibility(View.GONE);
					//Check with AccountNumber and change the image accordingly
					if (account.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
						imgCreditCard.setBackgroundResource(R.drawable.small_5);
					} else if (account.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
						imgCreditCard.setBackgroundResource(R.drawable.small_4);
					} else if (account.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
						imgCreditCard.setBackgroundResource(R.drawable.small_3);
					}
					imgCreditCardStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                    cc_available_funds.setTextColor(getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
					cc_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
				case "PL":
					linkedPersonalCardView.setVisibility(View.VISIBLE);
					applyPersonalCardView.setVisibility(View.GONE);
					imgPersonalLoanStatusIndicator.setVisibility(account.productOfferingGoodStanding ? View.GONE : View.VISIBLE);
                    pl_available_funds.setTextColor(getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
					pl_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
			}
		}

		//hide content for unavailable products
		boolean sc = true, cc = true, pl = true;
		for (String s : unavailableAccounts) {
			switch (s) {
				case "SC":
					applyStoreCardView.setVisibility(View.VISIBLE);
					linkedStoreCardView.setVisibility(View.GONE);
					cc = false;
					break;
				case "CC":
					applyCreditCardView.setVisibility(View.VISIBLE);
					linkedCreditCardView.setVisibility(View.GONE);
					sc = false;
					break;
				case "PL":
					applyPersonalCardView.setVisibility(View.VISIBLE);
					linkedPersonalCardView.setVisibility(View.GONE);
					pl = false;
					break;
			}
		}

		if (!sc && !cc && !pl) {
			hideView(linkedAccountsLayout);
			disableRefresh();
		}

		if (unavailableAccounts.size() == 0) {
			//all accounts are shown/linked
			applyNowAccountsLayout.setVisibility(View.GONE);
		} else {
			applyNowAccountsLayout.setVisibility(View.VISIBLE);
		}

		allUserOptionsLayout.setVisibility(View.VISIBLE);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(0);
		showFeatureWalkthroughPrompts();
	}

	private void disableRefresh() {
		if (mUpdateMyAccount != null)
			mUpdateMyAccount.enableSwipeToRefreshAccount(false);
		imRefreshAccount.setEnabled(false);
	}

	private void configureSignInNoC2ID() {
		this.configureAndLayoutTopLayerView();

		//show content for all available products
		for (Map.Entry<String, Account> item : accounts.entrySet()) {
			Account account = item.getValue();
			switch (account.productGroupCode) {
				case "SC":
					linkedStoreCardView.setVisibility(View.VISIBLE);
					applyStoreCardView.setVisibility(View.GONE);
					sc_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
				case "CC":
					linkedCreditCardView.setVisibility(View.VISIBLE);
					applyCreditCardView.setVisibility(View.GONE);
					//Check with AccountNumber and change the image accordingly
					if (account.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
						imgCreditCard.setBackgroundResource(R.drawable.small_5);
					} else if (account.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
						imgCreditCard.setBackgroundResource(R.drawable.small_4);
					} else if (account.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
						imgCreditCard.setBackgroundResource(R.drawable.small_3);
					}

					cc_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
				case "PL":
					showView(linkedPersonalCardView);
					hideView(applyPersonalCardView);

					pl_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
			}
		}

		//hide content for unavailable products
		for (String s : unavailableAccounts) {
			switch (s) {
				case "SC":
					showView(applyStoreCardView);
					hideView(linkedStoreCardView);
					break;
				case "CC":
					showView(applyCreditCardView);
					hideView(linkedCreditCardView);
					break;
				case "PL":
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
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(0);
		// prompts when user not linked
		isAccountsCallMade = true;
        showFeatureWalkthroughPrompts();
	}

	private void configureAndLayoutTopLayerView() {
		if (SessionUtilities.getInstance().isUserAuthenticated()) {
			showView(loggedInHeaderLayout);
			//logged in user's name and family name will be displayed on the page
			JWTDecodedModel jwtDecoded = SessionUtilities.getInstance().getJwt();
			String name = jwtDecoded.name.get(0);
			String familyName = jwtDecoded.family_name.get(0);
			userName.setText(name + " " + familyName);
			//initials of the logged in user will be displayed on the page
			showView(signOutRelativeLayout);
			showView(profileRelativeLayout);
            showView(updatePasswordRelativeLayout);
			showView(preferenceRelativeLayout);
			showView(loginUserOptionsLayout);
			mUpdateMyAccount.swipeToRefreshAccount(true);
			if (SessionUtilities.getInstance().isC2User())
				showView(linkedAccountsLayout);
			else {
				//user is not linked
				//but signed in
				mUpdateMyAccount.swipeToRefreshAccount(true);
				showView(unlinkedLayout);
				setUiPageViewController();
			}
		} else {
			//user is signed out
			mUpdateMyAccount.swipeToRefreshAccount(false);
			showView(loggedOutHeaderLayout);
			setUiPageViewController();
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
	}

	private void setUiPageViewController() {
		try {
			Activity activity = getActivity();
			pager_indicator.removeAllViews();
			dotsCount = adapter.getCount();
			dots = new ImageView[dotsCount];

			for (int i = 0; i < dotsCount; i++) {
				dots[i] = new ImageView(getActivity());
				if (activity != null)
					dots[i].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.my_account_page_indicator_default));

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);

				params.setMargins(10, 0, 10, 0);

				pager_indicator.addView(dots[i], params);
			}

			if (activity != null)
				dots[0].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.my_account_page_indicator_selected));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private View.OnClickListener btnSignin_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mUpdateMyAccount.accountUpdateActive()) return;
			Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSIGNIN);
			ScreenManager.presentSSOSignin(getActivity());
		}
	};

	private View.OnClickListener btnRegister_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mUpdateMyAccount.accountUpdateActive()) return;// disable tap to next view until account response completed
			Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSREGISTER);
			ScreenManager.presentSSORegister(getActivity());
		}
	};

	private View.OnClickListener btnLinkAccounts_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mUpdateMyAccount.accountUpdateActive()) return;
			ScreenManager.presentSSOLinkAccounts(getActivity());
		}
	};

	@Override
	public void onClick(View v) {
        Activity activity = getActivity();
        if (activity == null ||  mUpdateMyAccount.accountUpdateActive()) return;
		switch (v.getId()) {
			case R.id.openMessageActivity:
				Intent openMessageActivity = new Intent(getActivity(), MessagesActivity.class);
				openMessageActivity.putExtra("fromNotification", false);
				startActivity(openMessageActivity);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
			case R.id.applyStoreCard:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDAPPLYNOW);
				redirectToMyAccountsCardsActivity(ApplyNowState.STORE_CARD);
				break;
			case R.id.applyCrediCard:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSCREDITCARDAPPLYNOW);
				redirectToMyAccountsCardsActivity(ApplyNowState.GOLD_CREDIT_CARD);
				break;
			case R.id.applyPersonalLoan:
				Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANAPPLYNOW);
				redirectToMyAccountsCardsActivity(ApplyNowState.PERSONAL_LOAN);
				break;
			case R.id.linkedStoreCard:
				redirectToMyAccountsCardsActivity(0);
				break;
			case R.id.linkedCrediCard:
				redirectToMyAccountsCardsActivity(1);
				break;
			case R.id.linkedPersonalLoan:
				redirectToMyAccountsCardsActivity(2);
				break;
			case R.id.contactUs:
				pushFragment(new ContactUsFragment());
				break;
			case R.id.helpSection:
				HelpSectionFragment helpSectionFragment = new HelpSectionFragment();
				if (mAccountResponse != null) {
					Bundle bundle = new Bundle();
					bundle.putString("accounts", Utils.objectToJson(mAccountResponse));
					helpSectionFragment.setArguments(bundle);
				}
				pushFragment(helpSectionFragment);
				break;
			case R.id.signOutBtn:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.SIGN_OUT, "");
				break;
            case R.id.rlProfile:
                ScreenManager.presentSSOUpdateProfile(activity);
                break;
            case R.id.rlUpdatePassword:
                ScreenManager.presentSSOUpdatePassword(activity);
                break;
			case R.id.storeLocator:
				pushFragment(new StoresNearbyFragment1());
				break;
			case R.id.rlMyPreferences:
				startActivity(new Intent(getActivity(), MyPreferencesActivity.class));
				getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
				break;
			case R.id.imRefreshAccount:
				mUpdateMyAccount.setRefreshType(UpdateMyAccount.RefreshAccountType.CLICK_TO_REFRESH);
				loadAccounts(true);
				break;
			default:
				break;

		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		Activity activity  = getActivity();
		if (activity == null) return;
		for (int i = 0; i < dotsCount; i++) {
			dots[i].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.my_account_page_indicator_default));
		}
		dots[position].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.my_account_page_indicator_selected));
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

    private void loadAccounts(boolean forceNetworkUpdate) {
		if (!SessionUtilities.getInstance().isC2User()) return;
			mErrorHandlerView.hideErrorHandlerLayout();
			Activity activity = getActivity();
		if (activity != null)
			mScrollView.setBackgroundColor(ContextCompat.getColor(activity, R.color.recent_search_bg));
		if (forceNetworkUpdate)
			mUpdateMyAccount.swipeToRefreshAccount(true);
		else
			showProgressBar();
		mUpdateMyAccount.make(forceNetworkUpdate, new RequestListener<AccountsResponse>() {
            @Override
            public void onSuccess(AccountsResponse accountsResponse) {
                try {
                    int httpCode = accountsResponse.httpCode;
                    switch (httpCode) {
                        case 200:
                            mAccountResponse = accountsResponse;
                            List<Account> accountList = accountsResponse.accountList;
                            for (Account p : accountList) {
                                accounts.put(p.productGroupCode.toUpperCase(), p);
                                int indexOfUnavailableAccount = unavailableAccounts.indexOf(p.productGroupCode.toUpperCase());
                                if (indexOfUnavailableAccount > -1) {
                                    try {
                                        unavailableAccounts.remove(indexOfUnavailableAccount);
                                    } catch (Exception e) {
                                        Log.e("", e.getMessage());
                                    }
                                }
                            }
                            isAccountsCallMade = true;
                            configureView();
                            break;
                        case 440:
							mUpdateMyAccount.swipeToRefreshAccount(false);
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, accountsResponse.response.stsParams);
							if (activity != null)
								onSessionExpired(activity);
							initialize();
                            break;
                        default:
                            if (accountsResponse.response != null) {
								mUpdateMyAccount.swipeToRefreshAccount(false);
								if (activity != null)
									Utils.alertErrorMessage(activity, accountsResponse.response.desc);
                            }

                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                hideProgressBar();
				mUpdateMyAccount.swipeToRefreshAccount(false);
            }

            @Override
            public void onFailure(Throwable error) {
            	Activity activity = getActivity();
            	if (activity == null) return;
				activity.runOnUiThread(() -> {
					try {
						mUpdateMyAccount.swipeToRefreshAccount(false);
						hideProgressBar();
					} catch (Exception ignored) {
					}
					if (error != null)
						mErrorHandlerView.networkFailureHandler(error.getMessage());
				});

            }
        });
    }

	public void redirectToMyAccountsCardsActivity(int position) {
		Intent intent = new Intent(getActivity(), MyAccountCardsActivity.class);
		intent.putExtra("position", position);
		if (mAccountResponse != null) {
			intent.putExtra("accounts", Utils.objectToJson(mAccountResponse));
		}
		startActivityForResult(intent, 0);
		getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

	}

	@Override
	public void onDetach() {
		super.onDetach();
		if(getBottomNavigationActivity() != null & getBottomNavigationActivity().walkThroughPromtView != null){
			getBottomNavigationActivity().walkThroughPromtView.removeFromWindow();
		}

		if (mUpdateMyAccount!=null){
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
		if (activity != null) {
			// Enable message counter update if navigator points to account tab only
			BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
			Fragment currentFragment = bottomNavigationActivity.getCurrentFragment();
			if ((bottomNavigationActivity.getCurrentSection() == R.id.navigate_to_account)
					&& (currentFragment instanceof MyAccountsFragment)) {
				if (SessionUtilities.getInstance().isUserAuthenticated()
						&& SessionUtilities.getInstance().isC2User()) {
					getViewModel().loadMessageCount();
				}
			}
		}
	}

	@Override
	public void onShoppingListsResponse(ShoppingListsResponse shoppingListsResponse) {
	}

	@Override
	public void onMessageResponse(int unreadCount) {
		if (getActivity() == null) return;
		Utils.setBadgeCounter(unreadCount);
		addBadge(INDEX_ACCOUNT, unreadCount);
		if (unreadCount > 0) {
			hideView(getViewDataBinding().messagesRightArrow);
			showView(messageCounter);
			messageCounter.setText(String.valueOf(unreadCount));
		} else {
			Utils.removeBadgeCounter();
			hideView(messageCounter);
			showView(getViewDataBinding().messagesRightArrow);
		}
	}

	public void showProgressBar() {
		getViewDataBinding().pbAccount.bringToFront();
		showView(getViewDataBinding().pbAccount);
	}

	public void hideProgressBar() {
		hideView(getViewDataBinding().pbAccount);
	}

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
		//Check if view hierarchy was created
		if (!hidden && getViewDataBinding() != null) {
			//hide all views, load accounts may occur
			MyAccountsFragment.this.initialize();
			hideToolbar();
			setToolbarBackgroundColor(R.color.white);
			messageCounterRequest();

			//Fixes WOP-3407
			BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) getActivity();
			bottomNavigationActivity.showBottomNavigationMenu();
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
		} else if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
			initialize();
			//One time biometricsWalkthrough
			ScreenManager.presentBiometricWalkthrough(getActivity());
		} else if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
			onSignOut();
			clearActivityStoryStack();
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
		intent.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, String.valueOf(INDEX_ACCOUNT));
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

	@SuppressLint("StaticFieldLeak")
	private void showFeatureWalkthroughAccounts(List<String> unavailableAccounts) {
		if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.account)
			return;
		View viewToScrollUp = null;
		String actionText = getActivity().getResources().getString(R.string.tips_tricks_go_to_accounts);
		if (unavailableAccounts.size() == 3) {
			viewToScrollUp = imgStoreCardApplyNow;
			actionText = getActivity().getResources().getString(R.string.walkthrough_account_action_no_products);
		} else {
			if (!unavailableAccounts.contains("SC")) {
				viewToScrollUp = imgStoreCardContainer;
			} else if (!unavailableAccounts.contains("CC")) {
				viewToScrollUp = imgCreditCard;
			} else if (!unavailableAccounts.contains("PL")) {
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
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... voids) {
				Activity activity = getActivity();
				if (activity != null || isAdded()){
					activity.runOnUiThread(target::invalidate);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				Activity activity = getActivity();
				if (activity == null || !isAdded()) return;
				Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key),this.getClass().getCanonicalName());
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

	@Override
	public void onWalkthroughActionButtonClick() {
		switch (promptsActionListener) {
			case 1:
				if (unavailableAccounts.size() == 3) {
					onClick(applyStoreCardView);
				} else {
					if (!unavailableAccounts.contains("SC")) {
						onClick(linkedStoreCardView);
					} else if (!unavailableAccounts.contains("CC")) {
						onClick(linkedCreditCardView);
					} else if (!unavailableAccounts.contains("PL")) {
						onClick(linkedPersonalCardView);
					}
				}
				break;
		}
	}

	@Override
	public void onPromptDismiss() {
	}

	public View getTargetView(List<String> unavailableAccounts) {

		if (unavailableAccounts.size() == 3) {
			return getViewDataBinding().applyNowLayout.imgStoreCardApply;
		} else {
			if (!unavailableAccounts.contains("SC")) {
				return getViewDataBinding().linkedLayout.imgStoreCard;
			} else if (!unavailableAccounts.contains("CC")) {
				return getViewDataBinding().linkedLayout.imgCreditCardLayout;
			} else if (!unavailableAccounts.contains("PL")) {
				return getViewDataBinding().linkedLayout.imgPersonalLoan;
			}
		}
		return getViewDataBinding().applyNowLayout.imgStoreCardApply;
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

	public void redirectToMyAccountsCardsActivity(ApplyNowState applyNowState) {
		Activity activity = getActivity();
		if (activity == null) return;
		Intent intent = new Intent(getActivity(), AccountSalesActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("APPLY_NOW_STATE", applyNowState);
		intent.putExtras(bundle);
		startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}
}