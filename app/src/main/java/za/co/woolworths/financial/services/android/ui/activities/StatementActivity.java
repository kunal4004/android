package za.co.woolworths.financial.services.android.ui.activities;


import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.fragments.statement.AlternativeEmailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.EmailStatementFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class StatementActivity extends AppCompatActivity implements PermissionResultCallback {

	private final CompositeDisposable disposables = new CompositeDisposable();
	private WTextView mToolbarText;
	private PermissionUtils permissionUtils;
	ArrayList<String> permissions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_e_statement_activity);
		Utils.updateStatusBarBackground(this);
		actionBar();
		initUI();
		StatementFragment statementFragment = new StatementFragment();
		openNextFragment(statementFragment);
		permissionUtils = new PermissionUtils(this, this);
		permissions = new ArrayList<>();
		permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

		disposables.add(((WoolworthsApplication) StatementActivity.this.getApplication())
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						if (object instanceof AlternativeEmailFragment) {
							setTitle(getString(R.string.email_statements));
							AlternativeEmailFragment alternativeEmailFragment = new AlternativeEmailFragment();
							FragmentUtils fragmentUtils = new FragmentUtils(StatementActivity.this);
							fragmentUtils.nextFragment(StatementActivity.this, getSupportFragmentManager().beginTransaction(), alternativeEmailFragment, R.id.flEStatement);
						} else if (object instanceof EmailStatementFragment) {
							setTitle(getString(R.string.email_statements));
							EmailStatementFragment emailStatementFragment = new EmailStatementFragment();
							FragmentUtils fragmentUtils = new FragmentUtils(StatementActivity.this);
							fragmentUtils.nextFragment(StatementActivity.this, getSupportFragmentManager().beginTransaction(), emailStatementFragment, R.id.flEStatement);
						}
					}
				}));
	}

	private void actionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayUseLogoEnabled(false);
			actionBar.setHomeAsUpIndicator(R.drawable.back24);
		}
	}

	private void initUI() {
		mToolbarText = (WTextView) findViewById(R.id.toolbarText);
	}

	public void setTitle(String title) {
		mToolbarText.setText(title);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBack();
				return true;
		}
		return false;
	}


	private void openNextFragment(Fragment fragment) {
		FragmentUtils fragmentUtils = new FragmentUtils();
		fragmentUtils.currentFragment(StatementActivity.this, fragment, R.id.flEStatement);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!disposables.isDisposed())
			disposables.clear();
	}

	@Override
	public void onBackPressed() {
		onBack();
	}

	private void onBack() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else {
			finishActivity();
		}
	}

	public void finishActivity() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	@Override
	public void PermissionGranted(int request_code) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragmentId = fm.findFragmentById(R.id.flEStatement);
		if (fragmentId instanceof StatementFragment) {
			((StatementFragment) fragmentId).getPDFFile();
		}
	}

	@Override
	public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

	}

	@Override
	public void PermissionDenied(int request_code) {

	}

	@Override
	public void NeverAskAgain(int request_code) {

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// redirects to utils
		permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public void checkPermission() {
		permissionUtils.check_permission(permissions, "Explain here why the app needs permissions", 1);
	}
}
