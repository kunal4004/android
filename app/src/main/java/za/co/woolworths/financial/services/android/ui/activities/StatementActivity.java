package za.co.woolworths.financial.services.android.ui.activities;


import android.Manifest;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.awfs.coordination.R;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.fragments.statement.AlternativeEmailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.EmailStatementFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
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
	private Menu mMenu;
	private ActionBar actionBar;
	public static final String SEND_USER_STATEMENT = "SEND_USER_STATEMENT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_e_statement_activity);
		Utils.updateStatusBarBackground(this);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
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
						if (object instanceof BusStation) {
							BusStation busStation = (BusStation) object;
							showEmailStatementButton();
							Bundle alternativeEmailBundle = new Bundle();
							alternativeEmailBundle.putString(SEND_USER_STATEMENT, busStation.getString());
							AlternativeEmailFragment alternativeEmailFragment = new AlternativeEmailFragment();
							alternativeEmailFragment.setArguments(alternativeEmailBundle);
							FragmentUtils fragmentUtils = new FragmentUtils(StatementActivity.this);
							fragmentUtils.nextFragment(StatementActivity.this, getSupportFragmentManager().beginTransaction(), alternativeEmailFragment, R.id.flEStatement);
						} else if (object instanceof EmailStatementFragment) {
							showEmailStatementButton();
							EmailStatementFragment emailStatementFragment = new EmailStatementFragment();
							FragmentUtils fragmentUtils = new FragmentUtils(StatementActivity.this);
							fragmentUtils.nextFragment(StatementActivity.this, getSupportFragmentManager().beginTransaction(), emailStatementFragment, R.id.flEStatement);
						} else if (object instanceof StatementFragment) {
							finishActivity();
						}
					}
				}));
	}

	private void actionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayUseLogoEnabled(false);
			actionBar.setHomeAsUpIndicator(R.drawable.back24);
		}

	}

	public void showHomeButton() {
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setTitle(getString(R.string.statement));
		setGravity(Gravity.LEFT);
		hideCloseIcon();
	}

	public void showEmailStatementButton() {
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setTitle(getString(R.string.email_statements));
		setGravity(Gravity.LEFT);
		hideCloseIcon();

	}

	public void showAccountStatementButton() {
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
		setTitle(getString(R.string.account_statement));
		setGravity(Gravity.CENTER);
		showCloseIcon();
	}

	private void initUI() {
		mToolbarText = (WTextView) findViewById(R.id.toolbarText);
	}

	public void setTitle(String title) {
		mToolbarText.setText(title);
	}

	public void setGravity(int gravity) {
		mToolbarText.setGravity(gravity);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.itmIconClose:
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
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragmentId = fm.findFragmentById(R.id.flEStatement);
		if (fragmentId instanceof StatementFragment) {
			StatementFragment statementFragment = ((StatementFragment) fragmentId);
			if (statementFragment.isSlideUpPanelEnabled() == SlidingUpPanelLayout.PanelState.EXPANDED) {
				statementFragment.hideSlideUpPanel();
			} else {
				if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
					getSupportFragmentManager().popBackStack();
				} else {
					finishActivity();
				}
			}
		} else {
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				getSupportFragmentManager().popBackStack();
			} else {
				finishActivity();
			}
		}
	}

	public void finishActivity() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	@Override
	public void PermissionGranted(int request_code) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FragmentManager fm = getSupportFragmentManager();
				Fragment fragmentId = fm.findFragmentById(R.id.flEStatement);
				if (fragmentId instanceof StatementFragment) {
					((StatementFragment) fragmentId).getPDFFile();
				}
			}
		});
	}

	@Override
	public void PartialPermissionGranted(int request_code, ArrayList<
			String> granted_permissions) {

	}

	@Override
	public void PermissionDenied(int request_code) {

	}

	@Override
	public void NeverAskAgain(int request_code) {

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		// redirects to utils
		permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public void checkPermission() {
		permissionUtils.check_permission(permissions, "Explain here why the app needs permissions", 1);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.statement_menu_item, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.mMenu = menu;
		return true;
	}

	public void hideCloseIcon() {
		MenuItem menuItem = mMenu.findItem(R.id.itmIconClose);
		menuItem.setVisible(false);
	}

	public void showCloseIcon() {
		MenuItem menuItem = mMenu.findItem(R.id.itmIconClose);
		menuItem.setVisible(true);
	}

}
