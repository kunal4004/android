package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.FragmentLifecycle;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.ui.adapters.CardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;
import za.co.woolworths.financial.services.android.ui.views.WCustomPager;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WViewPager;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.PersonalLoanAmount;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;

public class MyAccountCardsActivity extends AppCompatActivity
		implements View.OnClickListener,
		PersonalLoanAmount {

	WViewPager pager;
	WCustomPager fragmentPager;
	public WTextView toolbarTextView;
	CardsFragmentPagerAdapter fragmentsAdapter;
	private WoolworthsApplication mWoolworthsApplication;
	private SharePreferenceHelper mSharePreferenceHelper;
	ArrayList<Integer> cards;
	private Toolbar mToolbar;
	private Button mBtnApplyNow;

	private boolean cardsHasAccount = false;
	private boolean containsStoreCard = false, containsCreditCard = false, containsPersonalLoan = false;
	private int wMinDrawnDownAmount;
	private LinearLayout llRootLayout;
	private AccountsResponse accountsResponse;
	private NestedScrollView mScrollAccountCard;
	int currentPosition = 0;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_accounts_offline_layout);
		setActionBar();
		init();
		mWoolworthsApplication = (WoolworthsApplication) MyAccountCardsActivity.this.getApplication();
		retryConnect();
		mSharePreferenceHelper = SharePreferenceHelper.getInstance(MyAccountCardsActivity.this);
		currentPosition = getIntent().getIntExtra("position", 0);
		fragmentPager = (WCustomPager) findViewById(R.id.fragmentpager);
		llRootLayout = (LinearLayout) findViewById(R.id.llRootLayout);
		fragmentPager.setViewPagerIsScrollable(false);
		cards = new ArrayList<>();
		changeViewPagerAndActionBarBackground(currentPosition);
		mBtnApplyNow.setVisibility(View.GONE);
		changeButtonColor(currentPosition);
		getScreenResolution();

		cardsHasAccount = getIntent().hasExtra("accounts");
		if (cardsHasAccount) {
			accountsResponse = new Gson().fromJson(getIntent().getExtras().getString("accounts"), AccountsResponse.class);
			handleAccountsResponse(accountsResponse);
		} else {
			fragmentsAdapter = new CardsFragmentPagerAdapter(getSupportFragmentManager()) {

				@Override
				public int getItemPosition(Object object) {

					return POSITION_NONE;
				}

			};
			fragmentsAdapter.addFrag(new WStoreCardEmptyFragment());
			fragmentsAdapter.addFrag(new WCreditCardEmptyFragment());
			fragmentsAdapter.addFrag(new WPersonalLoanEmptyFragment());
			fragmentPager.setAdapter(fragmentsAdapter);
			fragmentPager.setCurrentItem(getIntent().getIntExtra("position", 0));
			fragmentPager.setOffscreenPageLimit(2);

			cards.add(R.drawable.w_store_card);
			cards.add(R.drawable.creditcardbenfits);
			cards.add(R.drawable.w_personal_loan_card);
			setUpAdapter(cards);
		}


		mSharePreferenceHelper.save("acc_card_activity", "acc_card_activity");
		this.registerReceiver(this.finishAlert, new IntentFilter(mSharePreferenceHelper.getValue("acc_card_activity")));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			View decor = getWindow().getDecorView();

			decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
			// We want to change tint color to white again.
			// You can also record the flags in advance so that you can turn UI back completely if
			// you have set other flags before, such as translucent or full screen.
			//  decor.setSystemUiVisibility(0);
		}

		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int newPosition) {
				fragmentInterfaceListener(newPosition);
				mWoolworthsApplication.setCliCardPosition(newPosition);
				fragmentPager.setCurrentItem(newPosition);
				changeViewPagerAndActionBarBackground(newPosition);
				changeButtonColor(newPosition);
				setStatusBarColor(newPosition);

				currentPosition = newPosition;

			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		setStatusBarColor(currentPosition);
		disableScrollPageGesture();

		mScrollAccountCard.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (scrollY > oldScrollY) {
					hideViews();
				}
				if (scrollY < oldScrollY) {
					showViews();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		fragmentInterfaceListener(currentPosition);
	}

	private void retryConnect() {
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(MyAccountCardsActivity.this)) {
					Intent openAccount = new Intent(MyAccountCardsActivity.this,
							MyAccountCardsActivity.class);
					openAccount.putExtra("position", currentPosition);
					if (accountsResponse != null) {
						openAccount.putExtra("accounts", Utils.objectToJson(accountsResponse));
					}
					startActivity(openAccount);
					overridePendingTransition(0, 0);
					finish();
				}
			}

		});
	}

	private void disableScrollPageGesture() {
		pager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mWoolworthsApplication.getWGlobalState().cardGestureIsEnabled();
			}
		});
	}

	public void setActionBar() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(null);
			actionBar.setHomeAsUpIndicator(R.drawable.back_white);
		}
	}

	private void init() {
		mWoolworthsApplication = (WoolworthsApplication) MyAccountCardsActivity
				.this.getApplication();
		toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
		pager = (WViewPager) findViewById(R.id.myAccountsCardPager);

		mBtnApplyNow = (Button) findViewById(R.id.btnApplyNow);
		mScrollAccountCard = (NestedScrollView) findViewById(R.id.nest_scrollview);
		mBtnApplyNow.setOnClickListener(this);
	}

	BroadcastReceiver finishAlert = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			MyAccountCardsActivity.this.finish();
		}
	};

	private void dynamicToolbarColor(int color) {
		int mColor = ContextCompat.getColor(MyAccountCardsActivity.this, color);
		mToolbar.setBackgroundColor((mColor));
	}

	public void changeViewPagerAndActionBarBackground(int position) {
		switch (position) {
			case 0:
				toolbarTextView.setText("STORE CARD");
				pager.setBackgroundResource(R.drawable.accounts_storecard_background);
				dynamicToolbarColor(R.color.charcoal_grey);
				break;
			case 1:
				toolbarTextView.setText("CREDIT CARD");
				pager.setBackgroundResource(R.drawable.accounts_blackcreditcard_background);
				dynamicToolbarColor(R.color.cli_credit_card);
				break;
			case 2:
				toolbarTextView.setText("PERSONAL LOAN");
				pager.setBackgroundResource(R.drawable.accounts_personalloancard_background);
				dynamicToolbarColor(R.color.cli_personal_loan);
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSharePreferenceHelper.removeValue("acc_card_activity");
		this.unregisterReceiver(finishAlert);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	private void handleAccountsResponse(AccountsResponse accountsResponse) {
		switch (accountsResponse.httpCode) {
			case 200:

				((WoolworthsApplication) MyAccountCardsActivity.this.getApplication())
						.getUserManager
								().setAccounts
						(accountsResponse);
				List<Account> accountList = accountsResponse.accountList;

				if (accountList != null) {
					cards.clear();
					cards.add(R.drawable.w_store_card);
					for (Account p : accountList) {
						if ("SC".equals(p.productGroupCode)) {
							containsStoreCard = true;
						} else if ("CC".equals(p.productGroupCode)) {
							containsCreditCard = true;
							if (p.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
								cards.add(R.drawable.w_silver_credit_card);
							} else if (p.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
								cards.add(R.drawable.w_gold_credit_card);
							} else if (p.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
								cards.add(R.drawable.w_credi_card);
							}
						} else if ("PL".equals(p.productGroupCode)) {
							containsPersonalLoan = true;
						}
					}
					if (!containsCreditCard) {
						cards.add(R.drawable.creditcardbenfits);
					}
					cards.add(R.drawable.w_personal_loan_card);
					setUpAdapter(cards);
				}
				Bundle bundle = new Bundle();
				bundle.putString("accounts", Utils.objectToJson(accountsResponse));
				fragmentsAdapter = new CardsFragmentPagerAdapter(getSupportFragmentManager());

				if (containsStoreCard) {
					WStoreCardFragment fragment = new WStoreCardFragment();
					fragment.setArguments(bundle);
					fragmentsAdapter.addFrag(fragment);
				} else {
					fragmentsAdapter.addFrag(new WStoreCardEmptyFragment());
				}
				if (containsCreditCard) {
					WCreditCardFragment fragment = new WCreditCardFragment();
					fragment.setArguments(bundle);
					fragmentsAdapter.addFrag(fragment);
				} else {
					fragmentsAdapter.addFrag(new WCreditCardEmptyFragment());
				}
				if (containsPersonalLoan) {
					WPersonalLoanFragment fragment = new WPersonalLoanFragment();
					fragment.setArguments(bundle);
					fragmentsAdapter.addFrag(fragment);
				} else {
					fragmentsAdapter.addFrag(new WPersonalLoanEmptyFragment());
				}
				fragmentPager.setAdapter(fragmentsAdapter);
				fragmentPager.setCurrentItem(getIntent().getIntExtra("position", 0));

				changeButtonColor(currentPosition);

				break;
			case 400:
				if ("0619".equals(accountsResponse.response.code) || "0618".equals(accountsResponse.response.code)) {

					break;

				}
			case 502:
				Log.i("Handling 502", "Handled a 502 error from the server");
				break;
			default:
				((TextView) findViewById(R.id.no_internet_message)).setText(accountsResponse.response.desc);
				findViewById(R.id.no_internet).setVisibility(View.VISIBLE);
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == SSOActivity.SSOActivityResult.EXPIRED.rawValue()) {
			setResult(resultCode);
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnApplyNow:

				if (!cardsHasAccount) { //not logged in

					switch (pager.getCurrentItem()) {
						case 0:
							Utils.openExternalLink(MyAccountCardsActivity.this, WoolworthsApplication.getApplyNowLink());
							break;

						case 1:
							Utils.openExternalLink(MyAccountCardsActivity.this, WoolworthsApplication.getApplyNowLink());
							break;

						case 2:
							Utils.openExternalLink(MyAccountCardsActivity.this, WoolworthsApplication.getApplyNowLink());
							break;
					}

				} else {
					switch (pager.getCurrentItem()) { //logged in

						case 0:
							if (!containsStoreCard) {
								Utils.openExternalLink(MyAccountCardsActivity.this,
										WoolworthsApplication
												.getApplyNowLink());
							}
							break;

						case 1:
							if (!containsCreditCard) {
								Utils.openExternalLink(MyAccountCardsActivity.this,
										WoolworthsApplication
												.getApplyNowLink());

							}
							break;
						case 2:
							if (!containsPersonalLoan) {
								Utils.openExternalLink(MyAccountCardsActivity.this,
										WoolworthsApplication
												.getApplyNowLink());

							} else {
								mSharePreferenceHelper.save("", "lw_amount_drawn_cent");
								Intent openWithdrawCashNow = new Intent(MyAccountCardsActivity.this, LoanWithdrawalActivity.class);
								openWithdrawCashNow.putExtra("minDrawnDownAmount", wMinDrawnDownAmount);
								startActivity(openWithdrawCashNow);
								overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
							}
							break;
					}
				}
				break;
		}
	}

	@Override
	public void minDrawnAmount(int amount) {
		this.wMinDrawnDownAmount = amount;

	}

	public static class MyAccountCardsFragment extends Fragment {

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);

			getActivity().setResult(resultCode);
		}
	}

	public void setUpAdapter(ArrayList<Integer> cardsList) {
		pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivity.this, cardsList));
		pager.setCurrentItem(getIntent().getIntExtra("position", 0));
	}

	public void changeButtonColor(int position) {
		// not logged in
		mBtnApplyNow.setVisibility(View.GONE);
		if (!cardsHasAccount) {
			switch (position) {
				case 0:
					mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.cli_store_card));
					mBtnApplyNow.setVisibility(View.VISIBLE);
					break;
				case 1:
					mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.cli_credit_card));
					mBtnApplyNow.setVisibility(View.VISIBLE);
					break;
				case 2:
					mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.purple));
					mBtnApplyNow.setVisibility(View.VISIBLE);
					break;
			}
		} else { // logged in
			switch (position) {
				case 0:
					if (containsStoreCard) {
						mBtnApplyNow.setVisibility(View.GONE);
					} else {
						mBtnApplyNow.setText(getString(R.string.apply_now));
						mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.cli_store_card));
						mBtnApplyNow.setVisibility(View.VISIBLE);
					}
					break;
				case 1:
					if (containsCreditCard) {
						mBtnApplyNow.setVisibility(View.GONE);
					} else {
						mBtnApplyNow.setText(getString(R.string.apply_now));
						mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.cli_credit_card));
						mBtnApplyNow.setVisibility(View.VISIBLE);
					}
					break;
				case 2:
					if (containsPersonalLoan) {
						mBtnApplyNow.setVisibility(View.GONE);
					} else {
						mBtnApplyNow.setText(getString(R.string.apply_now));
						mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.purple));
						mBtnApplyNow.setVisibility(View.VISIBLE);
					}
					break;
			}
		}
	}

	private void hideViews() {
		mToolbar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
	}

	private void showViews() {
		mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
	}

	public void updateStatusBarBackground(int color) {

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			View decor = getWindow().getDecorView();
			window.setStatusBarColor(color);
			decor.setSystemUiVisibility(0);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(color);
		}

		llRootLayout.setBackgroundColor(color);
	}

	private void setStatusBarColor(int position) {
		switch (position) {
			case 0:
				int storeCardColor = ContextCompat.getColor(this, R.color.cli_store_card);
				updateStatusBarBackground(storeCardColor);
				break;

			case 1:
				int creditCardColor = ContextCompat.getColor(this, R.color.cli_credit_card);
				updateStatusBarBackground(creditCardColor);
				break;

			case 2:
				int personalLoanColor = ContextCompat.getColor(this, R.color.cli_personal_loan);
				updateStatusBarBackground(personalLoanColor);
				break;
		}
	}

	private void getScreenResolution() {
		int marginPx = getResources().getDimensionPixelSize(R.dimen.page_margin);
		int cardMarginPx = getResources().getDimensionPixelSize(R.dimen.card_margin);
		pager.setPadding(cardMarginPx, 0, cardMarginPx, 0);
		pager.setPageMargin(marginPx);
		pager.setClipToPadding(false);
	}

	private void fragmentInterfaceListener(int position) {
		try {
			FragmentLifecycle fragmentToShow = (FragmentLifecycle) fragmentsAdapter.getItem(position);
			fragmentToShow.onResumeFragment();

			FragmentLifecycle fragmentToHide = (FragmentLifecycle) fragmentsAdapter.getItem(position);
			fragmentToHide.onPauseFragment();
		} catch (Exception ignore) {

		}

	}
}
