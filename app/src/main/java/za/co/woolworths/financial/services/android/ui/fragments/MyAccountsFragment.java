package za.co.woolworths.financial.services.android.ui.fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.FAQActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.ShoppingListActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.activities.WChangePasswordActivity;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivityNew;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountOverViewPagerAdapter;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.ProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WObservableScrollView;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseFragment;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HideActionBar;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class MyAccountsFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener, ObservableScrollViewCallbacks {


	private HideActionBar hideActionBar;

	ImageView openMessageActivity;
	ImageView openShoppingList;
	RelativeLayout contactUs;

	LinearLayout applyCreditCardView;
	LinearLayout applyStoreCardView;
	LinearLayout applyPersonalCardView;
	LinearLayout linkedCreditCardView;
	LinearLayout linkedStoreCardView;
	LinearLayout linkedPersonalCardView;
	LinearLayout linkedAccountsLayout;
	LinearLayout applyNowAccountsLayout;
	LinearLayout loggedOutHeaderLayout;
	LinearLayout loggedInHeaderLayout;
	LinearLayout unlinkedLayout;
	WButton linkAccountsBtn;
	RelativeLayout signOutBtn;
	RelativeLayout changePasswordBtn;
	ViewPager viewPager;
	MyAccountOverViewPagerAdapter adapter;
	LinearLayout pager_indicator;

	WTextView sc_available_funds;
	WTextView cc_available_funds;
	WTextView pl_available_funds;
	WTextView messageCounter;
	WTextView userName;
	WTextView userInitials;

	private ProgressDialogFragment mGetAccountsProgressDialog;
	private ProgressBar scProgressBar;
	private ProgressBar ccProgressBar;
	private ProgressBar plProgressBar;

	private ImageView imgCreditCard;

	Map<String, Account> accounts;
	List<String> unavailableAccounts;
	private AccountsResponse accountsResponse; //purely referenced to be passed forward as Intent Extra


	private int dotsCount;
	private ImageView[] dots;
	private WObservableScrollView mWObservableScrollView;
	private Toolbar mToolbar;
	private RelativeLayout relFAQ;
	private ErrorHandlerView mErrorHandlerView;

	public MyAccountsFragment() {
		// Required empty public constructor
		this.accounts = new HashMap<>();
		this.unavailableAccounts = new ArrayList<>();
		this.accountsResponse = null;
	}

	WoolworthsApplication woolworthsApplication;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.my_accounts_fragment, container, false);
		woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
		openMessageActivity = (ImageView) view.findViewById(R.id.openMessageActivity);
		openShoppingList = (ImageView) view.findViewById(R.id.openShoppingList);
		contactUs = (RelativeLayout) view.findViewById(R.id.contactUs);
		applyStoreCardView = (LinearLayout) view.findViewById(R.id.applyStoreCard);
		applyCreditCardView = (LinearLayout) view.findViewById(R.id.applyCrediCard);
		applyPersonalCardView = (LinearLayout) view.findViewById(R.id.applyPersonalLoan);
		linkedCreditCardView = (LinearLayout) view.findViewById(R.id.linkedCrediCard);
		linkedStoreCardView = (LinearLayout) view.findViewById(R.id.linkedStoreCard);
		linkedPersonalCardView = (LinearLayout) view.findViewById(R.id.linkedPersonalLoan);
		linkedAccountsLayout = (LinearLayout) view.findViewById(R.id.linkedLayout);
		mWObservableScrollView = (WObservableScrollView) view.findViewById(R.id.nest_scrollview);
		applyNowAccountsLayout = (LinearLayout) view.findViewById(R.id.applyNowLayout);
		loggedOutHeaderLayout = (LinearLayout) view.findViewById(R.id.loggedOutHeaderLayout);
		loggedInHeaderLayout = (LinearLayout) view.findViewById(R.id.loggedInHeaderLayout);
		unlinkedLayout = (LinearLayout) view.findViewById(R.id.llUnlinkedAccount);
		linkAccountsBtn = (WButton) view.findViewById(R.id.linkAccountsBtn);
		signOutBtn = (RelativeLayout) view.findViewById(R.id.signOutBtn);
		changePasswordBtn = (RelativeLayout) view.findViewById(R.id.changePassword);
		viewPager = (ViewPager) view.findViewById(R.id.pager);
		pager_indicator = (LinearLayout) view.findViewById(R.id.viewPagerCountDots);
		sc_available_funds = (WTextView) view.findViewById(R.id.sc_available_funds);
		cc_available_funds = (WTextView) view.findViewById(R.id.cc_available_funds);
		pl_available_funds = (WTextView) view.findViewById(R.id.pl_available_funds);
		ImageView mImageView = (ImageView) view.findViewById(R.id.imgBurgerButton);
		scProgressBar = (ProgressBar) view.findViewById(R.id.scProgressBar);
		ccProgressBar = (ProgressBar) view.findViewById(R.id.ccProgressBar);
		plProgressBar = (ProgressBar) view.findViewById(R.id.plProgressBar);
		mToolbar = (Toolbar) view.findViewById(R.id.mToolbar);
		messageCounter = (WTextView) view.findViewById(R.id.messageCounter);
		userName = (WTextView) view.findViewById(R.id.user_name);
		userInitials = (WTextView) view.findViewById(R.id.initials);
		imgCreditCard = (ImageView) view.findViewById(R.id.imgCreditCard);
		relFAQ = (RelativeLayout) view.findViewById(R.id.relFAQ);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
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
		changePasswordBtn.setOnClickListener(this);
		mImageView.setOnClickListener(this);
		relFAQ.setOnClickListener(this);
		mWObservableScrollView.setScrollViewCallbacks(this);

		adapter = new MyAccountOverViewPagerAdapter(getActivity());
		viewPager.addOnPageChangeListener(this);
		setUiPageViewController();

		view.findViewById(R.id.loginAccount).setOnClickListener(this.btnSignin_onClick);
		view.findViewById(R.id.registerAccount).setOnClickListener(this.btnRegister_onClick);
		view.findViewById(R.id.linkAccountsBtn).setOnClickListener(this.btnLinkAccounts_onClick);
		showViews();
		//hide all views, load accounts may occur
		this.initialize();

		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline()) {
					loadAccounts();
				} else {
					mErrorHandlerView.showToast();
				}
			}

		});

		return view;
	}

	private void initialize() {
		this.accountsResponse = null;
		this.hideAllLayers();
		this.accounts.clear();
		this.unavailableAccounts.clear();
		this.unavailableAccounts.addAll(Arrays.asList("SC", "CC", "PL"));

		JWTDecodedModel jwtDecodedModel;
		try {
			jwtDecodedModel = ((WOneAppBaseActivity) getActivity()).getJWTDecoded();
		} catch (NullPointerException ignored) {
			jwtDecodedModel = null;
		}

		if (jwtDecodedModel != null && jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")) {
			this.loadAccounts();
		} else {
			this.configureView();
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
					scProgressBar.setProgress(getAvailableFundsPercentage(account.availableFunds, account.creditLimit));
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
					ccProgressBar.setProgress(getAvailableFundsPercentage(account.availableFunds, account.creditLimit));
					break;
				case "PL":
					linkedPersonalCardView.setVisibility(View.VISIBLE);
					applyPersonalCardView.setVisibility(View.GONE);

					pl_available_funds.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity())));
					plProgressBar.setProgress(getAvailableFundsPercentage(account.availableFunds, account.creditLimit));
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

		contactUs.setVisibility(View.VISIBLE);
		relFAQ.setVisibility(View.VISIBLE);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(0);
	}

	private void configureAndLayoutTopLayerView() {
		JWTDecodedModel jwtDecodedModel;
		try {
			jwtDecodedModel = ((WOneAppBaseActivity) getActivity()).getJWTDecoded();
		} catch (Exception ignored) {
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
				changePasswordBtn.setVisibility(View.VISIBLE);
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
		changePasswordBtn.setVisibility(View.GONE);
		linkedAccountsLayout.setVisibility(View.GONE);
		applyNowAccountsLayout.setVisibility(View.GONE);
		contactUs.setVisibility(View.GONE);
		relFAQ.setVisibility(View.GONE);
		unlinkedLayout.setVisibility(View.GONE);
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
		} catch (Exception ignored) {
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
				startActivity(new Intent(getActivity(), MessagesActivity.class).putExtra("fromNotification", false));
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
				startActivity(new Intent(getActivity(), WContactUsActivityNew.class));
				getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
				break;
			case R.id.relFAQ:
				startActivity(new Intent(getActivity(), FAQActivity.class));
				getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
				break;
			case R.id.openShoppingList:
				Intent openShoppingList = new Intent(getActivity(), ShoppingListActivity.class);
				startActivity(openShoppingList);
				getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;
			case R.id.signOutBtn:
				Utils.displayValidationMessage(getActivity(), TransientActivity.VALIDATION_MESSAGE_LIST.SIGN_OUT, "");
				break;
			case R.id.imgBurgerButton:
				hideActionBar.onBurgerButtonPressed();
				break;
			case R.id.changePassword:
				startActivity(new Intent(getActivity(), WChangePasswordActivity.class));
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
		FragmentManager fm = getActivity().getSupportFragmentManager();
		mGetAccountsProgressDialog = ProgressDialogFragment.newInstance();
		try {
			if (!mGetAccountsProgressDialog.isAdded()) {
				mGetAccountsProgressDialog.show(fm, "v");
			} else {
				mGetAccountsProgressDialog.dismiss();
				mGetAccountsProgressDialog = ProgressDialogFragment.newInstance();
				mGetAccountsProgressDialog.show(fm, "v");
			}

			accountAsyncRequest().execute();

		} catch (NullPointerException ignored) {
		}
	}

	private HttpAsyncTask<String, String, AccountsResponse> accountAsyncRequest() {
		return new HttpAsyncTask<String, String, AccountsResponse>() {

			@Override
			protected void onPreExecute() {
				mErrorHandlerView.hideErrorHandlerLayout();
				mWObservableScrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.recent_search_bg));
				relFAQ.setVisibility(View.GONE);
				showViews();
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
				switch (accountsResponse.httpCode) {
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
						AlertDialog mError = WErrorDialog.getSimplyErrorDialog(getActivity());
						mError.setTitle(getString(R.string.title_authentication_error));
						mError.setMessage(getString(R.string.session_out_message));
						mError.show();
						new android.os.AsyncTask<Void, Void, String>() {

							@Override
							protected String doInBackground(Void... params) {
								try {
									new SessionDao(getActivity(), SessionDao.KEY.USER_TOKEN).delete();
									new SessionDao(getActivity(), SessionDao.KEY.STORES_USER_SEARCH).delete();
									new SessionDao(getActivity(), SessionDao.KEY.STORES_USER_LAST_LOCATION).delete();
								} catch (Exception e) {
									e.printStackTrace();
								}
								return "";
							}

							@Override
							protected void onPostExecute(String s) {
								MyAccountsFragment.this.initialize();
							}
						}.execute();

						break;
					default:
						if (accountsResponse.response != null)
							Utils.alertErrorMessage(getActivity(), accountsResponse.response.desc);

						break;
				}
				dismissProgress();
			}
		};
	}

	public void redirectToMyAccountsCardsActivity(int position) {
		woolworthsApplication.setCliCardPosition(position);
		Intent intent = new Intent(getActivity(), MyAccountCardsActivity.class);

		intent.putExtra("position", position);
		if (accountsResponse != null) {
			intent.putExtra("accounts", Utils.objectToJson(accountsResponse));
		}
		startActivityForResult(intent, 0);
		getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

	}

	public void loadMessages() {
		new HttpAsyncTask<String, String, MessageResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected MessageResponse httpDoInBackground(String... params) {

				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getMessagesResponse(5, 1);
			}

			@Override
			protected Class<MessageResponse> httpDoInBackgroundReturnType() {
				return MessageResponse.class;
			}

			@Override
			protected MessageResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				MessageResponse messageResponse = new MessageResponse();
				messageResponse.response = new Response();
				return messageResponse;
			}

			@Override
			protected void onPostExecute(MessageResponse messageResponse) {

				super.onPostExecute(messageResponse);
				if (messageResponse.unreadCount > 0) {
					messageCounter.setVisibility(View.VISIBLE);
					int unreadCount = messageResponse.unreadCount;
					if (TextUtils.isEmpty(String.valueOf(unreadCount)))
						unreadCount = 0;
					Utils.setBadgeCounter(getActivity(), unreadCount);
					messageCounter.setText(String.valueOf(unreadCount));
				} else {
					Utils.removeBadgeCounter(getActivity());
					messageCounter.setVisibility(View.GONE);
				}
			}
		}.execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {

			initialize();
		} else if (resultCode == SSOActivity.SSOActivityResult.EXPIRED.rawValue()) {
			initialize();
		} else if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
			try {
				SessionDao sessionDao = new SessionDao(getActivity(), SessionDao.KEY.USER_TOKEN).get();
				sessionDao.value = "";
				sessionDao.save();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			initialize();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("UpdateCounter"));
		loadMessages();
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

	private void dismissProgress() {
		mWObservableScrollView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
		relFAQ.setVisibility(View.VISIBLE);
		if (mGetAccountsProgressDialog != null && mGetAccountsProgressDialog.isVisible()) {
			mGetAccountsProgressDialog.dismiss();
		}
	}

	public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			loadMessages();
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		((AppCompatActivity) getActivity()).getSupportActionBar().hide();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dismissProgress();
		((AppCompatActivity) getActivity()).getSupportActionBar().show();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			hideActionBar = (HideActionBar) getActivity();
		} catch (ClassCastException ignored) {
		}
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

	}

	@Override
	public void onDownMotionEvent() {

	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		if (scrollState.UP == scrollState) {
			hideViews();
		} else if (scrollState == scrollState.DOWN) {
			showViews();
		} else {
		}
	}

	private void hideViews() {
		mToolbar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
	}

	private void showViews() {
		mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
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
				dismissProgress();
			}
		});
	}


}