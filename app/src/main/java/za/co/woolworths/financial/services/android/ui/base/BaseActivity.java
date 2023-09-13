package za.co.woolworths.financial.services.android.ui.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.awfs.coordination.R;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.Utils;

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
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(false);
		}
	}

	public WoolworthsApplication woolworthsApplication() {
		return WoolworthsApplication.getInstance();
	}

	public void setBackNavigationIcon(boolean visibility) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (visibility) {
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setHomeButtonEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				getToolbar().setNavigationIcon(R.drawable.back24);
				getToolbar().setNavigationContentDescription(getString(R.string.back_button));
			} else {
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setHomeButtonEnabled(false);
				actionBar.setDisplayShowHomeEnabled(false);
				getToolbar().setNavigationIcon(null);
			}
		}
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	public void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	public Toolbar getToolbar() {
		return findViewById(R.id.incToolbar);
	}

	public WTextView getToolbarTitle() {
		return findViewById(R.id.tvToolbar);
	}

	public void setToolbarTitle(String title) {
		getToolbarTitle().setText(title);
		getToolbarTitle().setTextColor(ContextCompat.getColor(BaseActivity.this, R.color.black));
	}

	public void setToolbarTitle(String title, int color) {
		getToolbarTitle().setText(title);
		getToolbarTitle().setTextColor(ContextCompat.getColor(BaseActivity.this, color));
	}

	public void hideToolbar() {
		hideView(getToolbar());
	}

	public void setToolbarBackgroundColor(int colorId) {
		getToolbar().setBackgroundColor(ContextCompat.getColor(this, colorId));
	}

	public void setToolbarBackgroundDrawable(int drawableId) {
		getToolbar().setBackground(ContextCompat.getDrawable(this, drawableId));
	}

	public void showToolbar() {
		final Toolbar toolbar = getToolbar();
		toolbar.animate()
				.alpha(1.0f)
				.setDuration(0)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						showView(toolbar);
					}
				});
	}

	public void hideKeyboard() {
		View view = getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public WGlobalState getGlobalState() {
		return WoolworthsApplication.getInstance().getWGlobalState();
	}

	public void sendBus(Object obj) {
		Utils.sendBus(obj);
	}

	public boolean isNetworkConnected() {
		return NetworkManager.getInstance().isConnectedToNetwork(this);
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

	public boolean isEmpty(String value) {
		return TextUtils.isEmpty(value);
	}

	public void observableOn(Consumer consumer) {
		getViewModel().consumeObservable(consumer);
	}
}

