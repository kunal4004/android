package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WRewardsCardDetails;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.fragments.MenuNavigationInterface;
import za.co.woolworths.financial.services.android.ui.fragments.MyAccountsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.StoresNearbyFragment1;
import za.co.woolworths.financial.services.android.ui.fragments.WFragmentDrawer;
import za.co.woolworths.financial.services.android.ui.fragments.WProductFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WRewardsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WTodayFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HideActionBar;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.JWTHelper;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.UpdateNavDrawerTitle;
import za.co.woolworths.financial.services.android.util.UpdateNavigationDrawer;
import za.co.woolworths.financial.services.android.util.Utils;

public class WOneAppBaseActivity extends AppCompatActivity implements WFragmentDrawer.FragmentDrawerListener
		, WProductFragment.HideActionBarComponent, HideActionBar, UpdateNavDrawerTitle,
		WRewardsFragment.HideActionBarComponent, MenuNavigationInterface, UpdateNavigationDrawer {

	public static Toolbar mToolbar;
	private WFragmentDrawer drawerFragment;
	public WTextView mToolbarTitle;
	private List<Fragment> fragmentList;
	public static final String TAG = "WOneAppBaseActivity";

	private ActionBar mActionBar;
	private DrawerLayout mDrawerLayout;
	private WGlobalState mWGlobalState;
	private String mNotificationUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.one_app_base_activity);
		Utils.updateStatusBarBackground(this);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mWGlobalState = ((WoolworthsApplication) WOneAppBaseActivity.this.getApplication()).getWGlobalState();

		mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayShowHomeEnabled(false);
			mActionBar.setDisplayShowTitleEnabled(false); // false for hiding the title from actoinBar
		}

		mToolbarTitle = (WTextView) findViewById(R.id.toolbar_title);
		fragmentList = new ArrayList<>();

		mToolbar.setNavigationIcon(R.drawable.ic_drawer_menu);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		drawerFragment = (WFragmentDrawer)
				getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
		drawerFragment.setUp(R.id.fragment_navigation_drawer, mDrawerLayout, mToolbar);
		drawerFragment.setDrawerListener(this);
		displayView(Utils.DEFAULT_SELECTED_NAVIGATION_ITEM);
		registerReceiver(logOutReceiver, new IntentFilter("logOutReceiver"));
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mNotificationUtils = bundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT);
		}
		if (!TextUtils.isEmpty(mNotificationUtils)) {
			displayView(4);
		} else {
			if (bundle != null) {
				int mOpenProduct = bundle.getInt("myAccount");
				if (mOpenProduct == 1) {
					displayView(1);
				} else {
					displayView(Utils.DEFAULT_SELECTED_NAVIGATION_ITEM);
				}
			} else {
				displayView(Utils.DEFAULT_SELECTED_NAVIGATION_ITEM);
			}
		}

		initGetVouchersCall();
	}

	@Override
	public void onDrawerItemSelected(View view, int position) {
		displayView(position);
	}

	private void displayView(int position) {
		boolean isRewardFragment = false;
		Fragment fragment = null;
		String title = getString(R.string.app_name);
		switch (position) {
			case 0:
				fragment = new WTodayFragment();
				title = getString(R.string.nw_today_title);
				break;
			case 1:
				fragment = new WProductFragment();
				title = getString(R.string.nav_item_products);
				break;
			case 2:
				fragment = new StoresNearbyFragment1();
				title = getString(R.string.screen_title_store);
				break;
			case 3:
				mWGlobalState.setFragmentIsReward(true);
				mWGlobalState.resetStsParams();
				isRewardFragment = true;
				fragment = new WRewardsFragment();
				title = getString(R.string.wrewards);
				break;
			case 4:
				mWGlobalState.resetStsParams();
				mWGlobalState.setFragmentIsReward(false);
				fragment = new MyAccountsFragment();
				title = getString(R.string.nav_item_accounts);
				break;
		}

		try {
			if (fragment != null) {
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.container_body, fragment);
				fragmentTransaction.commit();
				// set the toolbar title
				mToolbarTitle.setText(title);
				fragmentList.add(fragment);
			}
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		for (Fragment fragment : fragmentList) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	public JWTDecodedModel getJWTDecoded() {
		JWTDecodedModel result = new JWTDecodedModel();
		try {
			SessionDao sessionDao = new SessionDao(WOneAppBaseActivity.this, SessionDao.KEY.USER_TOKEN).get();
			if (sessionDao.value != null && !sessionDao.value.equals("")) {
				result = JWTHelper.decode(sessionDao.value);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}


	public void hideActionBar(boolean actionbarIsVisible) {
		mToolbar.setVisibility(View.GONE);
	}

	@Override
	public void onBurgerButtonPressed() {
		if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			//drawer is open
			mDrawerLayout.openDrawer(Gravity.LEFT); //OPEN Nav Drawer!
		}
	}

	@Override
	public void onBackPressed() {
		if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			//drawer is open
			mDrawerLayout.openDrawer(Gravity.LEFT); //OPEN Nav Drawer!
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onTitleUpdate(String value) {
		mToolbarTitle.setText(value);
	}

	@Override
	public void onWRewardsDrawerPressed() {
		if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			//drawer is open
			mDrawerLayout.openDrawer(Gravity.LEFT); //OPEN Nav Drawer!
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(logOutReceiver);
		if (!TextUtils.isEmpty(mWGlobalState.getNewSTSParams())) {
			mWGlobalState.setRewardSignInState(false);
		}

	}

	BroadcastReceiver logOutReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ScreenManager.presentSSOLogout(WOneAppBaseActivity.this);
		}
	};

	@Override
	public void switchToView(int position) {
		displayView(position);
	}

	public void initGetVouchersCall() {
		JWTDecodedModel jwtDecodedModel = getJWTDecoded();
		if (jwtDecodedModel.AtgSession != null && jwtDecodedModel.C2Id != null && !jwtDecodedModel.C2Id.equals("")) {
			getVouchers().execute();
			new WRewardsCardDetails(WOneAppBaseActivity.this).execute();
		}
	}

	public HttpAsyncTask<String, String, VoucherResponse> getVouchers() {
		return new HttpAsyncTask<String, String, VoucherResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();

			}

			@Override
			protected VoucherResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getApplication()).getApi().getVouchers();
			}

			@Override
			protected Class<VoucherResponse> httpDoInBackgroundReturnType() {
				return VoucherResponse.class;
			}

			@Override
			protected VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				return new VoucherResponse();
			}

			@Override
			protected void onPostExecute(VoucherResponse voucherResponse) {
				super.onPostExecute(voucherResponse);
				int httpCode = voucherResponse.httpCode;
				switch (httpCode) {
					case 200:
						mWGlobalState.setRewardSignInState(true);
						List<Voucher> vouchers = voucherResponse.voucherCollection.vouchers;
						if (vouchers != null) {
							updateVoucherCount(vouchers.size());
						} else {
							updateVoucherCount(0);
						}
						break;

					case 440:
						updateVoucherCount(0);
						mWGlobalState.setRewardHasExpired(true);
						mWGlobalState.setRewardSignInState(false);
						break;

					default:
						updateVoucherCount(0);
						mWGlobalState.setRewardSignInState(false);
						break;
				}
			}
		};
	}

	@Override
	public void updateVoucherCount(int count) {
		drawerFragment.notifyNavigationDrawer(count);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}


