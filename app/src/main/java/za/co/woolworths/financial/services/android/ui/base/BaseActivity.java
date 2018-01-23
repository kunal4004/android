package za.co.woolworths.financial.services.android.ui.base;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;

public abstract class BaseActivity<T extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity implements BaseFragment.Callback {

	private T mViewDataBinding;
	private V mViewModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		performDataBinding();
	}

	private void performDataBinding() {
		mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
		mViewModel = mViewModel == null ? getViewModel() : mViewModel;
		mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
		mViewDataBinding.executePendingBindings();
	}

	@Override
	public void onFragmentAttached() {

	}

	@Override
	public void onFragmentDetached(String tag) {

	}

	/**
	 * Toolbar set up
	 */
	public void setActionBar() {
		setSupportActionBar(getToolbar());
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(false);
		}
	}

	public void setBackNavigationIcon(boolean visibility) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (visibility) {
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setHomeButtonEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				getToolbar().setNavigationIcon(R.drawable.back24);
			} else {
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setHomeButtonEnabled(false);
				actionBar.setDisplayShowHomeEnabled(false);
				getToolbar().setNavigationIcon(null);
			}
		}
	}

	public Toolbar getToolbar() {
		return findViewById(R.id.incToolbar);
	}

	private WTextView getToolbarTitle() {
		return findViewById(R.id.tvToolbar);
	}

	public void setToolbarTitle(String title) {
		getToolbarTitle().setText(title);
	}

	public void hideToolbar() {
		getToolbar().setVisibility(View.GONE);
	}

	public void showToolbar() {
		getToolbar().setVisibility(View.VISIBLE);
	}

	public void hideKeyboard() {
		View view = getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public boolean isNetworkConnected() {
		return new ConnectionDetector().isOnline(this);
	}

	public T getViewDataBinding() {
		return mViewDataBinding;
	}

	/**
	 * Override for set view model
	 *
	 * @return view model instance
	 */
	public abstract V getViewModel();

	/**
	 * Override for set binding variable
	 *
	 * @return variable id
	 */
	public abstract int getBindingVariable();

	/**
	 * @return layout resource id
	 */
	public abstract
	@LayoutRes
	int getLayoutId();
}

