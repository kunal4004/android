package za.co.woolworths.financial.services.android.ui.fragments.account;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.MyAccountsFragmentBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.GetShoppingLists;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.UserDetailActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.main_list.ContactUsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.faq.FAQFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_ACCOUNT;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_REWARD;

public class MyAccountsFragment extends BaseFragment<MyAccountsFragmentBinding, MyAccountsViewModel> implements View.OnClickListener, ViewPager.OnPageChangeListener, MyAccountsNavigator {

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
	RelativeLayout signOutBtn;
	RelativeLayout myDetailBtn;
	RelativeLayout myPreferences;
	ViewPager viewPager;
	MyAccountOverViewPagerAdapter adapter;
	LinearLayout pager_indicator;

	WTextView sc_available_funds;
	WTextView cc_available_funds;
	WTextView pl_available_funds;
	WTextView messageCounter;
	WTextView userName;
	private ImageView imgCreditCard;

	Map<String, Account> accounts;
	List<String> unavailableAccounts;
	private AccountsResponse accountsResponse; //purely referenced to be passed forward as Intent Extra

	private int dotsCount;
	private ImageView[] dots;
	private NestedScrollView mScrollView;
	private ErrorHandlerView mErrorHandlerView;
	private LinearLayout allUserOptionsLayout;
	private LinearLayout loginUserOptionsLayout;
	private GetShoppingLists mGetShoppingLists;
	private ShoppingListsResponse shoppingListsResponse;

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

		JWTDecodedModel jwtDecodedModel = SessionUtilities.getInstance().getJwt();
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
			RelativeLayout relMyList = view.findViewById(R.id.myLists);
			signOutBtn = view.findViewById(R.id.signOutBtn);
			myDetailBtn = view.findViewById(R.id.rlMyDetails);
			myPreferences = view.findViewById(R.id.rlMyPreferences);
			viewPager = view.findViewById(R.id.pager);
			pager_indicator = view.findViewById(R.id.viewPagerCountDots);
			sc_available_funds = view.findViewById(R.id.sc_available_funds);
			cc_available_funds = view.findViewById(R.id.cc_available_funds);
			pl_available_funds = view.findViewById(R.id.pl_available_funds);
			messageCounter = view.findViewById(R.id.messageCounter);
			userName = view.findViewById(R.id.user_name);
			imgCreditCard = view.findViewById(R.id.imgCreditCard);
			RelativeLayout relFAQ = view.findViewById(R.id.relFAQ);
			RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
			mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
			mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
			RelativeLayout storeLocator = view.findViewById(R.id.storeLocator);
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
			relMyList.setOnClickListener(this);
			adapter = new MyAccountOverViewPagerAdapter(getActivity());
			viewPager.addOnPageChangeListener(this);
			setUiPageViewController();

			view.findViewById(R.id.loginAccount).setOnClickListener(this.btnSignin_onClick);
			view.findViewById(R.id.registerAccount).setOnClickListener(this.btnRegister_onClick);
			view.findViewById(R.id.llUnlinkedAccount).setOnClickListener(this.btnLinkAccounts_onClick);
			myPreferences.setOnClickListener(this);

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
		this.mScrollView.scrollTo(0, 0);
		if (SessionUtilities.getInstance().isUserAuthenticated()) {
			if (SessionUtilities.getInstance().isC2User())
				this.loadAccounts();
			else
				this.configureSignInNoC2ID();
		} else {
			if (getActivity() == null) return;
			removeAllBottomNavigationIconBadgeCount();
			configureView();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!AppInstanceObject.biometricWalkthroughIsPresented(getActivity()))
			messageCounterRequest();
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
                    cc_available_funds.setTextColor(getResources().getColor(account.productOfferingGoodStanding ? R.color.black : R.color.black30));
					cc_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					break;
				case "PL":
					linkedPersonalCardView.setVisibility(View.VISIBLE);
					applyPersonalCardView.setVisibility(View.GONE);
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
		}

		showView(allUserOptionsLayout);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(0);
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
			showView(signOutBtn);
			showView(myDetailBtn);
			showView(myPreferences);
			showView(loginUserOptionsLayout);

			if (SessionUtilities.getInstance().isC2User())
				showView(linkedAccountsLayout);
			else {
				//user is not linked
				//but signed in
				showView(unlinkedLayout);
				setUiPageViewController();
			}
		} else {
			//user is signed out
			showView(loggedOutHeaderLayout);
			setUiPageViewController();
		}
	}

	private void hideAllLayers() {
		hideView(loggedInHeaderLayout);
		hideView(loggedOutHeaderLayout);
		hideView(signOutBtn);
		hideView(myDetailBtn);
		hideView(linkedAccountsLayout);
		hideView(applyNowAccountsLayout);
		hideView(allUserOptionsLayout);
		hideView(unlinkedLayout);
		hideView(loginUserOptionsLayout);
		hideView(myPreferences);
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
				startActivity(openMessageActivity);
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
			case R.id.myLists:
				Bundle bundle = new Bundle();
				if (shoppingListsResponse != null) {
					bundle.putString("ShoppingList", Utils.objectToJson(shoppingListsResponse));
					ShoppingListFragment shoppingListFragment = new ShoppingListFragment();
					shoppingListFragment.setArguments(bundle);
					pushFragment(shoppingListFragment);
				}
				break;
			case R.id.rlMyPreferences:
				startActivity(new Intent(getActivity(), MyPreferencesActivity.class));
				getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

	@SuppressLint("StaticFieldLeak")
	private HttpAsyncTask<String, String, AccountsResponse> accountAsyncRequest() {
		return new HttpAsyncTask<String, String, AccountsResponse>() {

			@Override
			protected void onPreExecute() {
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
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, accountsResponse.response.stsParams);
							onSessionExpired(getActivity());
							initialize();
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
	public void onDestroy() {
		super.onDestroy();
		hideProgressBar();
		cancelRequest(mGetShoppingLists);
	}

//	public int getAvailableFundsPercentage(int availableFund, int creditLimit) {
//		// Progressbar MAX value is 10000 to manage float values
//		int percentage = Math.round((100 * ((float) availableFund / (float) creditLimit)) * 100);
//		if (percentage < 0 || percentage > Utils.ACCOUNTS_PROGRESS_BAR_MAX_VALUE)
//			return Utils.ACCOUNTS_PROGRESS_BAR_MAX_VALUE;
//		else
//			return percentage;
//	}

	public void networkFailureHandler() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					hideProgressBar();
				} catch (Exception ignored) {
				}
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private void onSignOut() {
		try {
			AsyncTask<Void, Void, Void> httpAsyncTask = new HttpAsyncTask<Void, Void, Void>() {
				@Override
				protected Void httpDoInBackground(Void... params) {
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
				protected Void httpError(String errorMessage, HttpErrorCode httpErrorCode) {
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					mScrollView.scrollTo(0, 0);
				}

				@Override
				protected Class<Void> httpDoInBackgroundReturnType() {
					return null;
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
					getViewModel().loadMessageCount().execute();
				}
			}
		}
	}

	private void shoppingListRequest() {
		if (SessionUtilities.getInstance().isUserAuthenticated()) {
			mGetShoppingLists = getViewModel().getShoppingListsResponse();
			mGetShoppingLists.execute();
		}
	}

	@Override
	public void onShoppingListsResponse(ShoppingListsResponse shoppingListsResponse) {
		this.shoppingListsResponse = shoppingListsResponse;
		if (shoppingListsResponse.lists != null && shoppingListsResponse.lists.size() > 0) {
			hideView(getViewDataBinding().myListRightArrow);
			showView(getViewDataBinding().listsCounter);
			getViewDataBinding().listsCounter.setText(String.valueOf(shoppingListsResponse.lists.size()));
		} else {
			hideView(getViewDataBinding().listsCounter);
			showView(getViewDataBinding().myListRightArrow);
		}
	}

	@Override
	public void onMessageResponse(int unreadCount) {
		Activity activity = getActivity();
		if (activity == null) return;
		Utils.setBadgeCounter(activity, unreadCount);
		addBadge(INDEX_ACCOUNT, unreadCount);
		if (unreadCount > 0) {
			hideView(getViewDataBinding().messagesRightArrow);
			showView(messageCounter);
			messageCounter.setText(String.valueOf(unreadCount));
		} else {
			Utils.removeBadgeCounter(getActivity());
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
		if (!hidden) {
			//do when hidden
			//hide all views, load accounts may occur
			this.initialize();
			hideToolbar();
			setToolbarBackgroundColor(R.color.white);
			shoppingListRequest();
			messageCounterRequest();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//TODO: Comment what's actually happening here.
		if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
			shoppingListRequest();
			initialize();
			//One time biometricsWalkthrough
			ScreenManager.presentBiometricWalkthrough(getActivity());
		} else if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
			onSignOut();
			initialize();
		} else {
			initialize();
		}
	}

	private void removeAllBottomNavigationIconBadgeCount() {
		addBadge(INDEX_REWARD, 0);
		addBadge(INDEX_ACCOUNT, 0);
		addBadge(INDEX_CART, 0);
		resetShoppingListAndMessagesUI();
	}

	private void onSessionExpired(Activity activity) {
		Utils.setBadgeCounter(getActivity(), 0);
		removeAllBottomNavigationIconBadgeCount();
		SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
		SessionExpiredUtilities.getInstance().showSessionExpireDialog((AppCompatActivity) activity);
		initialize();
	}

	private void resetShoppingListAndMessagesUI() {
		if (getActivity() != null) {
			hideView(getViewDataBinding().listsCounter);
			showView(getViewDataBinding().myListRightArrow);
			hideView(messageCounter);
			showView(getViewDataBinding().messagesRightArrow);
		}
	}

	public void scrollToTop() {
		ObjectAnimator anim = ObjectAnimator.ofInt(mScrollView, "scrollY", mScrollView.getScrollY(), 0);
		anim.setDuration(500).start();
	}
}