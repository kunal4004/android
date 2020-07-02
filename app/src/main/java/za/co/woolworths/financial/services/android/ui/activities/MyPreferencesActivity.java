package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class MyPreferencesActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

	private Toolbar mToolbar;
	private Switch authenticateSwitch;
	private static final int LOCK_REQUEST_CODE_TO_ENABLE = 222;
	private static final int LOCK_REQUEST_CODE_TO_DISABLE = 333;
	private static final int SECURITY_SETTING_REQUEST_CODE = 232;
	public static final int SECURITY_SETTING_REQUEST_DIALOG = 234;
	public static final int SECURITY_INFO_REQUEST_DIALOG = 235;
	private LinearLayout biometricsLayout;
	private RelativeLayout rlLocationSelectedLayout;
	private WTextView tvDeliveryLocation;
	private WTextView tvDeliveringToText;
	private String mSuburbName, mProvinceName;
	private static final int REQUEST_SUBURB_CHANGE = 143;
	private ImageView imRightArrow;
	private ImageView imDeliveryLocationIcon;
	private WTextView tvEditDeliveryLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.my_preferences_activity);
		init();
		setActionBar();
		bindDataWithUI();
	}

	private void setActionBar() {
		setSupportActionBar(mToolbar);
		ActionBar mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayUseLogoEnabled(false);
			mActionBar.setHomeAsUpIndicator(R.drawable.back24);
		}
	}

	private void init() {
		mToolbar = findViewById(R.id.mToolbar);
		authenticateSwitch = findViewById(R.id.auSwitch);
		biometricsLayout = findViewById(R.id.biometricsLayout);
		tvDeliveryLocation = findViewById(R.id.tvDeliveryLocation);
		tvDeliveringToText = findViewById(R.id.tvDeliveringTo);
		rlLocationSelectedLayout = findViewById(R.id.locationSelectedLayout);
		tvEditDeliveryLocation = findViewById(R.id.editLocation);
		imRightArrow = findViewById(R.id.iconCaretRight);
		imDeliveryLocationIcon = findViewById(R.id.deliverLocationIcon);
		authenticateSwitch.setOnClickListener(this);
		rlLocationSelectedLayout.setOnClickListener(this);
		authenticateSwitch.setOnTouchListener(this);
	}

	public void bindDataWithUI() {

		if (AuthenticateUtils.getInstance(MyPreferencesActivity.this).isAppSupportsAuthentication()) {
			if (AuthenticateUtils.getInstance(MyPreferencesActivity.this).isDeviceSecure())
				authenticateSwitch.setChecked(AuthenticateUtils.getInstance(MyPreferencesActivity.this).isAuthenticationEnabled());
			else
				setUserAuthentication(false);
		} else {
			biometricsLayout.setVerticalGravity(View.GONE);
		}

		ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
		if (lastDeliveryLocation != null) {
			mSuburbName = lastDeliveryLocation.suburb.name;
			mProvinceName = lastDeliveryLocation.province.name;
			setDeliveryLocation(mSuburbName, mProvinceName);
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.auSwitch:
				if (AuthenticateUtils.getInstance(MyPreferencesActivity.this).isDeviceSecure()) {
					if (authenticateSwitch.isChecked()) {
						startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE);
					} else {
						Utils.displayValidationMessageForResult(MyPreferencesActivity.this, CustomPopUpWindow.MODAL_LAYOUT.BIOMETRICS_SECURITY_INFO, getString(R.string.biometrics_security_info), SECURITY_INFO_REQUEST_DIALOG);
					}
				} else
					openDeviceSecuritySettings();
				break;
			case R.id.locationSelectedLayout:
				locationSelectionClicked();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case LOCK_REQUEST_CODE_TO_ENABLE:
				setUserAuthentication(resultCode == RESULT_OK ? true : false);
				if (resultCode == RESULT_OK) {
					AuthenticateUtils.getInstance(MyPreferencesActivity.this).enableBiometricForCurrentSession(false);
				}
				break;
			case LOCK_REQUEST_CODE_TO_DISABLE:
				setUserAuthentication(resultCode == RESULT_OK ? false : true);
				break;
			case SECURITY_SETTING_REQUEST_CODE:
				if (AuthenticateUtils.getInstance(MyPreferencesActivity.this).isDeviceSecure()) {
					startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE);
				} else {
					setUserAuthentication(false);
				}
				break;
			case REQUEST_SUBURB_CHANGE:
				ShoppingDeliveryLocation lastDeliveryLocation = Utils.getPreferredDeliveryLocation();
				if (lastDeliveryLocation != null) {
					mSuburbName = lastDeliveryLocation.suburb.name;
					mProvinceName = lastDeliveryLocation.province.name;
					setDeliveryLocation(mSuburbName, mProvinceName);
				}
				break;
			case SECURITY_SETTING_REQUEST_DIALOG:
				if (resultCode == RESULT_OK) {
					try {
						Intent intent = new Intent(Settings.ACTION_SETTINGS);
						startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
					} catch (Exception ex) {
						setUserAuthentication(false);
					}
				} else {
					setUserAuthentication(false);
				}
				break;
			case SECURITY_INFO_REQUEST_DIALOG:
				startBiometricAuthentication(LOCK_REQUEST_CODE_TO_DISABLE);
				break;
			default:
				break;
		}
	}

	public void startBiometricAuthentication(int requestCode) {
		try {
			AuthenticateUtils.getInstance(MyPreferencesActivity.this).startAuthenticateApp(requestCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUserAuthentication(boolean isAuthenticated) {
		AuthenticateUtils.getInstance(MyPreferencesActivity.this).setUserAuthenticate(isAuthenticated ? SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON : SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF);
		authenticateSwitch.setChecked(isAuthenticated);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finishActivity();
				return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		finishActivity();
	}

	private void finishActivity() {
		finish();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	public void openDeviceSecuritySettings() {
		Utils.displayValidationMessageForResult(MyPreferencesActivity.this, CustomPopUpWindow.MODAL_LAYOUT.SET_UP_BIOMETRICS_ON_DEVICE, "", SECURITY_SETTING_REQUEST_DIALOG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.PREFERENCES);
		bindDataWithUI();
	}

	private void locationSelectionClicked() {
		Intent openDeliveryLocationSelectionActivity = new Intent(MyPreferencesActivity.this, DeliveryLocationSelectionActivity.class);
		openDeliveryLocationSelectionActivity.putExtra("suburbName", mSuburbName);
		openDeliveryLocationSelectionActivity.putExtra("provinceName", mProvinceName);
		startActivityForResult(openDeliveryLocationSelectionActivity, REQUEST_SUBURB_CHANGE);
		overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (view.getId()) {
			case R.id.auSwitch:
				return motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE;
			default:
				break;
		}
		return false;
	}

	public void setDeliveryLocation(String suburb, String provinceName) {
		if (TextUtils.isEmpty(suburb) || suburb.equalsIgnoreCase("null")) return;
		imRightArrow.setVisibility(View.GONE);
		tvDeliveringToText.setVisibility(View.VISIBLE);
		tvEditDeliveryLocation.setVisibility(View.VISIBLE);
		imDeliveryLocationIcon.setBackgroundResource(R.drawable.tick_cli_active);
		tvDeliveringToText.setText(provinceName);
		tvDeliveryLocation.setVisibility(View.VISIBLE);
		tvDeliveryLocation.setText(suburb);
	}
}
