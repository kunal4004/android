package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class BiometricsWalkthrough extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

	public FrameLayout closeWindow;
	private Switch authenticateSwitch;
	private static final int LOCK_REQUEST_CODE_TO_ENABLE = 222;
	private static final int SECURITY_SETTING_REQUEST_CODE = 232;
	public static final int SECURITY_SETTING_REQUEST_DIALOG = 234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.walkthrought_biometrics);
		init();
	}

	public void init() {
		closeWindow = findViewById(R.id.closeWindow);
		closeWindow.setOnClickListener(this);
		authenticateSwitch = findViewById(R.id.auSwitch);
		authenticateSwitch.setOnClickListener(this);
		// One time Biometrics walkthrough
		this.oneTimeWalkthroughPresented();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.closeWindow:
				finishActivity();
				break;
			case R.id.auSwitch:
				if (AuthenticateUtils.getInstance(BiometricsWalkthrough.this).isDeviceSecure()&& authenticateSwitch.isChecked()) {
						startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE);
				} else {
					openDeviceSecuritySettings();
				}
				break;
			default:
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case LOCK_REQUEST_CODE_TO_ENABLE:
				setUserAuthentication(resultCode == RESULT_OK ? true : false);
				if (resultCode == RESULT_OK) {
					AuthenticateUtils.getInstance(BiometricsWalkthrough.this).enableBiometricForCurrentSession(false);
					finishActivity();
				}
				break;
			case SECURITY_SETTING_REQUEST_CODE:
				if (AuthenticateUtils.getInstance(BiometricsWalkthrough.this).isDeviceSecure()) {
					startBiometricAuthentication(LOCK_REQUEST_CODE_TO_ENABLE);
				} else {
					setUserAuthentication(false);
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
			default:
				break;
		}
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

	public void startBiometricAuthentication(int requestCode) {
		try {
			AuthenticateUtils.getInstance(BiometricsWalkthrough.this).startAuthenticateApp(requestCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUserAuthentication(boolean isAuthenticated) {
		AuthenticateUtils.getInstance(BiometricsWalkthrough.this).setUserAuthenticate(isAuthenticated ? SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON : SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF);
		authenticateSwitch.setChecked(isAuthenticated);
	}

	public void openDeviceSecuritySettings() {
		Utils.displayValidationMessageForResult(BiometricsWalkthrough.this, CustomPopUpWindow.MODAL_LAYOUT.SET_UP_BIOMETRICS_ON_DEVICE, "", SECURITY_SETTING_REQUEST_DIALOG);
	}

	@Override
	public void onBackPressed() {
		finishActivity();
	}

	private void finishActivity() {
		finish();
	}

	public void oneTimeWalkthroughPresented(){
		AppInstanceObject appInstanceObject = AppInstanceObject.get();
		appInstanceObject.setBiometricWalkthroughPresented(true);
		appInstanceObject.save();
	}
}
