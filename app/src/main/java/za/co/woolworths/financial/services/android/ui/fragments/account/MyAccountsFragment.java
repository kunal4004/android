package za.co.woolworths.financial.services.android.ui.fragments.account;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.awfs.coordination.databinding.MyAccountsFragmentBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WRewardsCardDetails;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.message.CLIGetMessageResponse;
import za.co.woolworths.financial.services.android.models.rest.reward.GetVoucher;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.ShoppingListActivity;
import za.co.woolworths.financial.services.android.ui.activities.UserDetailActivity;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.main_list.ContactUsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.faq.FAQFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import com.awfs.coordination.BR;

public class MyAccountsFragment extends BaseFragment<MyAccountsFragmentBinding, MyAccountsViewModel> implements View.OnClickListener, ViewPager.OnPageChangeListener, MyAccountsNavigator {

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
	RelativeLayout signOutBtn;
	RelativeLayout myDetailBtn;
	ViewPager viewPager;
	MyAccountOverViewPagerAdapter adapter;
	LinearLayout pager_indicator;

	WTextView sc_available_funds;
	WTextView cc_available_funds;
	WTextView pl_available_funds;
	WTextView messageCounter;
	WTextView userName;
	WTextView userInitials;
	private ImageView imgCreditCard;

	Map<String, Account> accounts;
	List<String> unavailableAccounts;
	private AccountsResponse accountsResponse; //purely referenced to be passed forward as Intent Extra


	private int dotsCount;
	private ImageView[] dots;
	private NestedScrollView mScrollView;
	private RelativeLayout relFAQ;
	private ErrorHandlerView mErrorHandlerView;
	private WGlobalState wGlobalState;
	private boolean loadMessageCounter = false;
	private String TAG = "MyAccountsFragment";
	private RelativeLayout storeLocator;
	private LinearLayout allUserOptionsLayout;
	private LinearLayout loginUserOptionsLayout;

	public MyAccountsFragment() {
		// Required empty public constructor
		this.accounts = new HashMap<>();
		this.unavailableAccounts = new ArrayList<>();
		this.accountsResponse = null;
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
		JWTDecodedModel jwtDecodedModel = Utils.getJWTDecoded(getActivity());
		Map<String, String> arguments = new HashMap<>();
		arguments.put("c2_id", (jwtDecodedModel.C2Id != null) ? jwtDecodedModel.C2Id : "");
		Utils.triggerFireBaseEvents(getActivity(), "accounts_event_appeared", arguments);
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
			wGlobalState = woolworthsApplication.getWGlobalState();
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
			signOutBtn = view.findViewById(R.id.signOutBtn);
			myDetailBtn = view.findViewById(R.id.rlMyDetails);
			viewPager = view.findViewById(R.id.pager);
			pager_indicator = view.findViewById(R.id.viewPagerCountDots);
			sc_available_funds = view.findViewById(R.id.sc_available_funds);
			cc_available_funds = view.findViewById(R.id.cc_available_funds);
			pl_available_funds = view.findViewById(R.id.pl_available_funds);
			messageCounter = view.findViewById(R.id.messageCounter);
			userName = view.findViewById(R.id.user_name);
			userInitials = view.findViewById(R.id.initials);
			imgCreditCard = view.findViewById(R.id.imgCreditCard);
			relFAQ = view.findViewById(R.id.relFAQ);
			RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
			mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
			mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
			storeLocator = view.findViewById(R.id.storeLocator);
			allUserOptionsLayout = view.findViewById(R.id.parentOptionsLayout);
			loginUserOptionsLayout = view.findViewById(R.id.loginUserOptionsLayout);
			openMessageActivity.setOnClickListener(this);
			contactUs.setOnClickListener(this);
			applyPersonalCardView.setOnClickListener(this);
			applyStoreCardView.setOnClickListener(this);
			applyCreditCardView.setOnClickListener(this);
			linkedStoreCardView.setOnClickListener(this);
			linkedCreditCardView.setOnClickListener(this);
			linkedPersonalCardView.setOnClickListener(this);
			openShoppingList.setOnClickListener(this);
			signOutBtn.setOnClickListener(this);
			myDetailBtn.setOnClickListener(this);
			relFAQ.setOnClickListener(this);
			storeLocator.setOnClickListener(this);

			adapter = new MyAccountOverViewPagerAdapter(getActivity());
			viewPager.addOnPageChangeListener(this);
			setUiPageViewController();

			view.findViewById(R.id.loginAccount).setOnClickListener(this.btnSignin_onClick);
			view.findViewById(R.id.registerAccount).setOnClickListener(this.btnRegister_onClick);
			view.findViewById(R.id.llUnlinkedAccount).setOnClickListener(this.btnLinkAccounts_onClick);
			//hide all views, load accounts may occur
			this.initialize();

			view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (new ConnectionDetector().isOnline(getActivity())) {
						loadAccounts();
					}
				}

			});
		}
	}

	private void initialize() {
		this.accountsResponse = null;
		this.hideAllLayers();
		this.accounts.clear();
		this.unavailableAccounts.clear();
		this.unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));

		JWTDecodedModel jwtDecodedModel;
		try {
			jwtDecodedModel = Utils.getJWTDecoded(getActivity());
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			jwtDecodedModel = null;
		}

		if (wGlobalState.getAccountSignInState()) {
			if (jwtDecodedModel != null && jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")) {
				this.loadAccounts();
			} else {
				this.configureSignInNoC2ID();
			}
		} else {
			this.configureView();
			if (!wGlobalState.getRewardSignInState() || wGlobalState.rewardHasExpired()) {
				//Remove voucher count on Navigation drawer
				//updateNavigationDrawer.onSuccess(0);
			}
		}
	}

	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
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
					linkedPersonalCardView.setVisibility(View.VISIBLE);
					applyPersonalCardView.setVisibility(View.GONE);

					pl_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
			}
		}

		//hide content for unavailable products
		for (String s : unavailableAccounts) {
			switch (s) {
				case "SC":
					applyStoreCardView.setVisibility(View.VISIBLE);
					linkedStoreCardView.setVisibility(View.GONE);
					break;
				case "CC":
					applyCreditCardView.setVisibility(View.VISIBLE);
					linkedCreditCardView.setVisibility(View.GONE);
					break;
				case "PL":
					applyPersonalCardView.setVisibility(View.VISIBLE);
					linkedPersonalCardView.setVisibility(View.GONE);
					break;
			}
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

		// not login sign in
		if (!wGlobalState.getAccountSignInState())
			showLogOutScreen();
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
					linkedPersonalCardView.setVisibility(View.VISIBLE);
					applyPersonalCardView.setVisibility(View.GONE);

					pl_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
			}
		}

		//hide content for unavailable products
		for (String s : unavailableAccounts) {
			switch (s) {
				case "SC":
					applyStoreCardView.setVisibility(View.VISIBLE);
					linkedStoreCardView.setVisibility(View.GONE);
					break;
				case "CC":
					applyCreditCardView.setVisibility(View.VISIBLE);
					linkedCreditCardView.setVisibility(View.GONE);
					break;
				case "PL":
					applyPersonalCardView.setVisibility(View.VISIBLE);
					linkedPersonalCardView.setVisibility(View.GONE);
					break;
			}
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
	}


	private void configureAndLayoutTopLayerView() {
		JWTDecodedModel jwtDecodedModel;
		try {
			jwtDecodedModel = Utils.getJWTDecoded(getActivity());
		} catch (Exception ex) {
			ex.printStackTrace();
			jwtDecodedModel = null;
		}

		if (jwtDecodedModel != null) {
			if (jwtDecodedModel.AtgSession != null) {
				loggedInHeaderLayout.setVisibility(View.VISIBLE);
				//logged in user's name and family name will be displayed on the page
				userName.setText(jwtDecodedModel.name.get(0) + " " + jwtDecodedModel.family_name.get(0));
				//initials of the logged in user will be displayed on the page
				String initials = jwtDecodedModel.name.get(0).substring(0, 1).concat(" ").concat(jwtDecodedModel.family_name.get(0).substring(0, 1));
				userInitials.setText(initials);
				signOutBtn.setVisibility(View.VISIBLE);
				myDetailBtn.setVisibility(View.VISIBLE);
				loginUserOptionsLayout.setVisibility(View.VISIBLE);
				if (jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")) {
					//user is linked and signed in
					linkedAccountsLayout.setVisibility(View.VISIBLE);
				} else {
					//user is not linked
					//but signed in
					unlinkedLayout.setVisibility(View.VISIBLE);
					setUiPageViewController();
				}
			} else {
				//user is signed out
				loggedOutHeaderLayout.setVisibility(View.VISIBLE);
				setUiPageViewController();
			}
		} else {
			//user is signed out
			loggedOutHeaderLayout.setVisibility(View.VISIBLE);
			setUiPageViewController();
		}
	}

	private void hideAllLayers() {
		loggedInHeaderLayout.setVisibility(View.GONE);
		loggedOutHeaderLayout.setVisibility(View.GONE);
		signOutBtn.setVisibility(View.GONE);
		myDetailBtn.setVisibility(View.GONE);
		linkedAccountsLayout.setVisibility(View.GONE);
		applyNowAccountsLayout.setVisibility(View.GONE);
		allUserOptionsLayout.setVisibility(View.GONE);
		unlinkedLayout.setVisibility(View.GONE);
		loginUserOptionsLayout.setVisibility(View.GONE);
	}

	private void setUiPageViewController() {
		try {
			pager_indicator.removeAllViews();
			dotsCount = adapter.getCount();
			dots = new ImageView[dotsCount];

			for (int i = 0; i < dotsCount; i++) {
				dots[i] = new ImageView(getActivity());
				dots[i].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.my_account_page_indicator_default));

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);

				params.setMargins(10, 0, 10, 0);

				pager_indicator.addView(dots[i], params);
			}

			dots[0].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.my_account_page_indicator_selected));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private View.OnClickListener btnSignin_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ScreenManager.presentSSOSignin(getActivity());
		}
	};

	private View.OnClickListener btnRegister_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ScreenManager.presentSSORegister(getActivity());
		}
	};

	private View.OnClickListener btnLinkAccounts_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ScreenManager.presentSSOLinkAccounts(getActivity());
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.openMessageActivity:
				Intent openMessageActivity = new Intent(getActivity(), MessagesActivity.class);
				openMessageActivity.putExtra("fromNotification", false);
				startActivityForResult(openMessageActivity, 0);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
			case R.id.applyStoreCard:
				redirectToMyAccountsCardsActivity(0);
				break;
			case R.id.applyCrediCard:
				redirectToMyAccountsCardsActivity(1);
				break;
			case R.id.applyPersonalLoan:
				redirectToMyAccountsCardsActivity(2);
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
			case R.id.relFAQ:
				pushFragment(new FAQFragment());
				break;
			case R.id.openShoppingList:
				Intent openShoppingList = new Intent(getActivity(), ShoppingListActivity.class);
				startActivity(openShoppingList);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
			case R.id.signOutBtn:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.SIGN_OUT, "");
				break;

			case R.id.rlMyDetails:
				Intent openMyDetail = new Intent(getActivity(), UserDetailActivity.class);
				startActivity(openMyDetail);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
			case R.id.storeLocator:
				pushFragment(new StoresNearbyFragment1());
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
		for (int i = 0; i < dotsCount; i++) {
			dots[i].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.my_account_page_indicator_default));
		}
		dots[position].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.my_account_page_indicator_selected));
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	private void loadAccounts() {
		accountAsyncRequest().execute();
	}

	private HttpAsyncTask<String, String, AccountsResponse> accountAsyncRequest() {
		return new HttpAsyncTask<String, String, AccountsResponse>() {

			@Override
			protected void onPreExecute() {
				loadMessageCounter = false;
				mErrorHandlerView.hideErrorHandlerLayout();
				mScrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.recent_search_bg));
				showProgressBar();
			}

			@Override
			protected Class<AccountsResponse> httpDoInBackgroundReturnType() {
				return AccountsResponse.class;
			}

			@Override
			protected AccountsResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getAccounts();
			}

			@Override
			protected AccountsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler();
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new AccountsResponse();
			}

			@Override
			protected void onPostExecute(AccountsResponse accountsResponse) {
				try {
					int httpCode = accountsResponse.httpCode;
					switch (httpCode) {
						case 200:
							loadMessageCounter = false;
							wGlobalState.setAccountHasExpired(false);
							MyAccountsFragment.this.accountsResponse = accountsResponse;
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
							configureView();
							break;
						case 440:
							loadMessageCounter = false;
							accounts.clear();
							unavailableAccounts.clear();
							unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));
							wGlobalState.setAccountHasExpired(true);
							configureView();
							Utils.setBadgeCounter(getActivity(), 0);
							showLogOutScreen();
							wGlobalState.setDefaultPopupState(true);
							SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), accountsResponse.response.stsParams);
							SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
							break;
						default:
							if (accountsResponse.response != null) {
								Utils.alertErrorMessage(getActivity(), accountsResponse.response.desc);
							}

							break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				hideProgressBar();
			}
		};
	}

	public void redirectToMyAccountsCardsActivity(int position) {
		Intent intent = new Intent(getActivity(), MyAccountCardsActivity.class);
		intent.putExtra("position", position);
		if (accountsResponse != null) {
			intent.putExtra("accounts", Utils.objectToJson(accountsResponse));
		}
		startActivityForResult(intent, 0);
		getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("UpdateCounter"));
		messageCounterRequest();
		try {
			onSessionExpired();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
		} catch (Exception e) {
			Log.e(TAG, "Broadcast Unregister Exception");
		}
	}

	public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			messageCounterRequest();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideProgressBar();
	}

	public int getAvailableFundsPercentage(int availableFund, int creditLimit) {
		// Progressbar MAX value is 10000 to manage float values
		int percentage = Math.round((100 * ((float) availableFund / (float) creditLimit)) * 100);

		if (percentage < 0 || percentage > Utils.ACCOUNTS_PROGRESS_BAR_MAX_VALUE)
			return Utils.ACCOUNTS_PROGRESS_BAR_MAX_VALUE;
		else
			return percentage;
	}

	public void networkFailureHandler() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					hideProgressBar();
				} catch (Exception ex) {
				}
			}
		});
	}

	public void showLogOutScreen() {
		applyCreditCardView.setVisibility(View.VISIBLE);
		applyStoreCardView.setVisibility(View.VISIBLE);
		applyPersonalCardView.setVisibility(View.VISIBLE);
		loggedOutHeaderLayout.setVisibility(View.VISIBLE);
		loggedInHeaderLayout.setVisibility(View.GONE);
		linkedAccountsLayout.setVisibility(View.GONE);
		myDetailBtn.setVisibility(View.GONE);
		signOutBtn.setVisibility(View.GONE);
		loginUserOptionsLayout.setVisibility(View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
			wGlobalState.setAccountSignInState(true);
			if (loadMessageCounter) {
				messageCounterRequest();
			} else {
				initialize();
				//getVouchers().execute();
				if (getActivity() != null)
					new WRewardsCardDetails(getActivity()).execute();
			}
		} else if (resultCode == SSOActivity.SSOActivityResult.EXPIRED.rawValue()) {
			wGlobalState.setAccountSignInState(false);
			initialize();
			showLogOutScreen();
			addBadge(BottomNavigationActivity.INDEX_REWARD, 0);
			addBadge(BottomNavigationActivity.INDEX_ACCOUNT, 0);
		} else if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
			try {
				wGlobalState.setAccountSignInState(false);
				wGlobalState.setRewardSignInState(false);
				SessionDao sessionDao = new SessionDao(getActivity(), SessionDao.KEY.USER_TOKEN).get();
				sessionDao.value = "";
				sessionDao.save();
				addBadge(BottomNavigationActivity.INDEX_REWARD, 0);
				addBadge(BottomNavigationActivity.INDEX_ACCOUNT, 0);
				new HttpAsyncTask<Void, Void, Void>() {

					@Override
					protected Void httpDoInBackground(Void... params) {
						try {
							new SessionDao(getActivity(), SessionDao.KEY.STORES_USER_SEARCH).delete();
							new SessionDao(getActivity(), SessionDao.KEY.STORES_USER_LAST_LOCATION).delete();
						} catch (Exception pE) {
							pE.printStackTrace();
						}
						return null;
					}

					@Override
					protected Void httpError(String errorMessage, HttpErrorCode httpErrorCode) {
						return null;
					}

					@Override
					protected Class<Void> httpDoInBackgroundReturnType() {
						return null;
					}
				}.execute();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			initialize();
		} else {
			addBadge(BottomNavigationActivity.INDEX_REWARD, 0);
			addBadge(BottomNavigationActivity.INDEX_ACCOUNT, 0);
			//user not signed in
			if (!wGlobalState.getAccountSignInState()) {
				this.accounts.clear();
				this.unavailableAccounts.clear();
				this.unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));
				this.configureView();
			}
		}
	}

	private void onSessionExpired() {
		if (!TextUtils.isEmpty(wGlobalState.getNewSTSParams())
				&& !wGlobalState.getDefaultPopupState()) {
			loadMessageCounter = false;
			accounts.clear();
			unavailableAccounts.clear();
			unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));
			wGlobalState.setAccountHasExpired(true);

			configureView();
			showLogOutScreen();
			SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
		} else {
			if (wGlobalState.accountHasExpired()
					&& (wGlobalState.getPressState().equalsIgnoreCase
					(WGlobalState.ON_CANCEL))) {
				accountExpiredState();
			} else if (wGlobalState.accountHasExpired()
					&& (wGlobalState.getPressState().equalsIgnoreCase
					(WGlobalState.ON_SIGN_IN))) {
				accountExpiredState();
				//mNavigationInterface.switchToFragment(4);
			} else {
			}
		}
	}

	private void accountExpiredState() {
		wGlobalState.setAccountHasExpired(false);
		wGlobalState.setPressState("");
	}

	private void messageCounterRequest() {
		getMessageResponse().execute();
		getWRewards().execute();
	}

	private CLIGetMessageResponse getMessageResponse() {
		return new CLIGetMessageResponse(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				MessageResponse messageResponse = (MessageResponse) object;
				if (messageResponse.unreadCount > 0) {
					messageCounter.setVisibility(View.VISIBLE);
					int unreadCount = messageResponse.unreadCount;
					if (TextUtils.isEmpty(String.valueOf(unreadCount)))
						unreadCount = 0;
					Utils.setBadgeCounter(getActivity(), unreadCount);
					messageCounter.setText(String.valueOf(unreadCount));
					getBottomNavigator().addBadge(BottomNavigationActivity.INDEX_ACCOUNT, unreadCount);
				} else {
					Utils.removeBadgeCounter(getActivity());
					getBottomNavigator().addBadge(BottomNavigationActivity.INDEX_ACCOUNT, 0);
					messageCounter.setVisibility(View.GONE);
				}
			}

			@Override
			public void onFailure(String errorMessage) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
			}
		});
	}

	private GetVoucher getWRewards() {
		return new GetVoucher(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				try {
					if (object != null) {
						VoucherResponse voucherResponse = ((VoucherResponse) object);
						if (voucherResponse != null) {
							VoucherCollection voucherCollection = voucherResponse.voucherCollection;
							if (voucherCollection != null) {
								List<Voucher> voucher = voucherCollection.vouchers;
								if (!voucher.isEmpty()) {
									getBottomNavigator().addBadge(BottomNavigationActivity.INDEX_REWARD, voucher.size());
								}
							}
						}
					}
				} catch (IllegalStateException ignored) {
				}
			}

			@Override
			public void onFailure(String errorMessage) {
			}
		});
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
		if (!hidden) {
			//do when hidden
			hideToolbar();
			setToolbarBackgroundColor(R.color.white);
		}
	}
}