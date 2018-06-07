package za.co.woolworths.financial.services.android.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.awfs.coordination.R;

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
	private LinearLayout biometricsLayout;
	private RelativeLayout rlLocationSelectedLayout;
	private WTextView tvDeliveryLocation;
	private WTextView tvDeliveringToText;
	private String mSuburbName, mProvinceName;
	private static final int REQUEST_SUBURB_CHANGE = 143;

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

		ShoppingDeliveryLocation lastDeliveryLocation = Utils.getLastDeliveryLocation(MyPreferencesActivity.this);
		if (lastDeliveryLocation != null) {
			mSuburbName = lastDeliveryLocation.suburb.name;
			mProvinceName = lastDeliveryLocation.province.name;
			setDeliveryLocation(mSuburbName + ", " + mProvinceName);
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.auSwitch:
				if (AuthenticateUtils.getInstance(MyPreferencesActivity.this).isDeviceSecure())
					startBiometricAuthentication(authenticateSwitch.isChecked() ? LOCK_REQUEST_CODE_TO_ENABLE : LOCK_REQUEST_CODE_TO_DISABLE);
				else
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
				if(resultCode == RESULT_OK){
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
				ShoppingDeliveryLocation lastDeliveryLocation = Utils.getLastDeliveryLocation(MyPreferencesActivity.this);
				if (lastDeliveryLocation != null) {
					mSuburbName = lastDeliveryLocation.suburb.name;
					mProvinceName = lastDeliveryLocation.province.name;
					setDeliveryLocation(mSuburbName + ", " + mProvinceName);
				}
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

	public void openDeviceSecuritySettings(){
		new AlertDialog.Builder(this)
				.setTitle("Use Authentication ?")
				.setMessage("Thia app want to change your device settings:")
				.setCancelable(false)
				.setPositiveButton(R.string.cli_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							Intent intent = new Intent(Settings.ACTION_SETTINGS);
							startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
						} catch (Exception ex) {
							setUserAuthentication(false);
						}
					}
				})
				.setNegativeButton(R.string.cli_no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						setUserAuthentication(false);
					}
				})
				.show();

	}

	@Override
	protected void onResume() {
		super.onResume();
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
		switch (view.getId()){
			case  R.id.auSwitch :
				return motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE;
			default:
				break;
		}
		return false;
	}

	public void setDeliveryLocation(String deliveryLocation) {
		tvDeliveringToText.setText(getResources().getString(R.string.delivering_to));
		tvDeliveryLocation.setVisibility(View.VISIBLE);
		tvDeliveryLocation.setText(deliveryLocation);
	}
}
