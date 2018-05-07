package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class MyPreferencesActivity extends AppCompatActivity implements View.OnClickListener {

	private Toolbar mToolbar;
	private Switch authenticateSwitch;
	private static final int LOCK_REQUEST_CODE_TO_ENABLE = 222;
	private static final int LOCK_REQUEST_CODE_TO_DISABLE = 333;
	private RelativeLayout biometricsLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.my_preferences_activity);
		init();
		setActionBar();
		bindDateWithUI();
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
		authenticateSwitch.setOnClickListener(this);
	}

	public void bindDateWithUI() {
		if (AuthenticateUtils.getInstance(MyPreferencesActivity.this).isAppSupportsAuthentication()) {
			biometricsLayout.setVisibility(View.VISIBLE);
			authenticateSwitch.setChecked(AuthenticateUtils.getInstance(MyPreferencesActivity.this).isAuthenticationEnabled());
		} else {
			biometricsLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.auSwitch:
				startBiometricAuthentication(authenticateSwitch.isChecked() ? LOCK_REQUEST_CODE_TO_ENABLE : LOCK_REQUEST_CODE_TO_DISABLE);
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
				break;
			case LOCK_REQUEST_CODE_TO_DISABLE:
				setUserAuthentication(resultCode == RESULT_OK ? false : true);
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
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}
}
