package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.adapters.UserDetailAdapter;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class UserDetailActivity extends AppCompatActivity implements UserDetailAdapter.UserDetailInterface, View.OnClickListener {

	private Toolbar mToolbar;
	private RecyclerView mRclUserDetail;
	private UserDetailAdapter mUserDetailApdater;
	private Switch authenticateSwitch;
	private static final int LOCK_REQUEST_CODE_TO_ENABLE = 222;
	private static final int LOCK_REQUEST_CODE_TO_DISABLE = 333;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.user_detail_layout);
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
		mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		mRclUserDetail = (RecyclerView) findViewById(R.id.rclUserDetail);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRclUserDetail.setLayoutManager(mLayoutManager);
		authenticateSwitch = findViewById(R.id.auSwitch);
		authenticateSwitch.setOnClickListener(this);
	}

	public void bindDateWithUI() {
		mUserDetailApdater = new UserDetailAdapter(getItem(), this);
		mRclUserDetail.setAdapter(mUserDetailApdater);
		authenticateSwitch.setChecked(AuthenticateUtils.getInstance(UserDetailActivity.this).isAuthenticationEnabled());
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

	private List<String> getItem() {
		List<String> mConstruct = new ArrayList<>();
		mConstruct.add(getXmlString(R.string.acc_profile));
		//mConstruct.add(getXmlString(R.string.acc_update_email_address));
		mConstruct.add(getXmlString(R.string.acc_update_password));
		return mConstruct;
	}

	private String getXmlString(int id) {
		return getResources().getString(id);
	}

	@Override
	public void onRowSelected(View v, int position) {
		switch (position) {
			case 0:
				ScreenManager.presentSSOUpdateProfile(UserDetailActivity.this);
				break;

			case 1:
				ScreenManager.presentSSOUpdatePassword(UserDetailActivity.this);
				break;

			default:
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mUserDetailApdater.resetIndex();
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
			AuthenticateUtils.getInstance(UserDetailActivity.this).startAuthenticateApp(requestCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUserAuthentication(boolean isAuthenticated) {
		AuthenticateUtils.getInstance(UserDetailActivity.this).setUserAuthenticate(isAuthenticated ? SessionDao.BIOMETRIC_AUTHENTICATION_STATE.ON : SessionDao.BIOMETRIC_AUTHENTICATION_STATE.OFF);
		authenticateSwitch.setChecked(isAuthenticated);
	}
}
