package za.co.woolworths.financial.services.android.ui.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.awfs.coordination.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.service.event.BusStation;
import za.co.woolworths.financial.services.android.ui.fragments.statement.AlternativeEmailFragment;
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class StatementActivity extends AppCompatActivity {

	private final CompositeDisposable disposables = new CompositeDisposable();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_e_statement_activity);
		Utils.updateStatusBarBackground(this);
		actionBar();
		initUI();
		StatementFragment statementFragment = new StatementFragment();
		openNextFragment(statementFragment);
		disposables.add(((WoolworthsApplication) StatementActivity.this.getApplication())
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						if (object instanceof AlternativeEmailFragment) {
							AlternativeEmailFragment alternativeEmailFragment = new AlternativeEmailFragment();
							FragmentUtils fragmentUtils = new FragmentUtils(StatementActivity.this);
							fragmentUtils.nextFragment(StatementActivity.this, getSupportFragmentManager().beginTransaction(), alternativeEmailFragment, R.id.flEStatement);
						} else if (object instanceof BusStation) {

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
}
