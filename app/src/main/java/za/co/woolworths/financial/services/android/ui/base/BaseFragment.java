package za.co.woolworths.financial.services.android.ui.base;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;

public abstract class BaseFragment<T extends ViewDataBinding, V extends BaseViewModel> extends Fragment {

	public final int PERMS_REQUEST_CODE = 123;

	private BaseActivity mActivity;
	private T mViewDataBinding;
	private V mViewModel;
	private View mRootView;
	private BottomNavigator bottomNavigator;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = getViewModel();
		setHasOptionsMenu(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {            // Inflate and populate
		mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
		mRootView = mViewDataBinding.getRoot();
		Activity activity = getActivity();
		if (activity != null) {
			try {
				bottomNavigator = (BottomNavigator) activity;
			} catch (IllegalStateException ex) {
			}
		}
		return mRootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
		mViewDataBinding.executePendingBindings();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof BaseActivity) {
			BaseActivity activity = (BaseActivity) context;
			mActivity = activity;
			//activity.onFragmentAttached();
		}
	}

	public boolean hasPermissions() {
		int res;
		//string array of permissions,
		String[] permissions = new String[]{Manifest.permission.CAMERA};

		for (String perms : permissions) {
			res = getActivity().checkCallingOrSelfPermission(perms);
			if (!(res == PackageManager.PERMISSION_GRANTED)) {
				return false;
			}
		}
		return true;
	}

	public void requestPerms() {
		String[] permissions = new String[]{Manifest.permission.CAMERA};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(permissions, PERMS_REQUEST_CODE);
		}
	}


	@Override
	public void onDetach() {
		mActivity = null;
		super.onDetach();
	}

	public BaseActivity getBaseActivity() {
		return mActivity;
	}

	public T getViewDataBinding() {
		return mViewDataBinding;
	}

	public boolean isNetworkConnected() {
		return new ConnectionDetector().isOnline(mActivity);
	}

	public void hideToolbar() {
		mActivity.hideToolbar();
	}

	public void showToolbar() {
		Utils.updateStatusBarBackground(getActivity());
		mActivity.showToolbar();
	}

	public void hideKeyboard() {
		if (mActivity != null) {
			//mActivity.hideKeyboard();
		}
	}

	public interface Callback {

		void onFragmentAttached();

		void onFragmentDetached(String tag);
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


	public int getToolBarHeight() {
		int[] attrs = new int[]{R.attr.actionBarSize};
		TypedArray ta = getContext().obtainStyledAttributes(attrs);
		int toolBarHeight = ta.getDimensionPixelSize(0, -1);
		ta.recycle();
		return toolBarHeight;
	}

	public void showBackNavigationIcon(boolean visibility) {
		bottomNavigator.showBackNavigationIcon(visibility);
	}

	public void setTitle(String title) {
		bottomNavigator.setTitle(title);
	}

	public void setToolbarBackgroundDrawable(int drawable) {
		mActivity.setToolbarBackgroundDrawable(drawable);
	}

	public void setToolbarBackgroundColor(int color) {
		mActivity.setToolbarBackgroundColor(color);
	}

	public BottomNavigator getBottomNavigator() {
		return bottomNavigator;
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	public void hideView(View view) {
		if (view != null) {
			view.setVisibility(View.GONE);
		}
	}

	public void slideBottomPanel() {
		bottomNavigator.slideUpBottomView();
	}

	public boolean isEmpty(String value) {
		return TextUtils.isEmpty(value);
	}

	public void pushFragment(Fragment fragment) {
		getBottomNavigator().pushFragment(fragment);
	}

	public void setText(WTextView tv, String text) {
		tv.setText(text);
	}

	public void setText(String text, WTextView tv) {
		if (isEmpty(text)) {
			hideView(tv);
		} else {
			tv.setText(text);
			showView(tv);
		}
	}

	public void fadeOutToolbar(int color) {
		getBottomNavigator().fadeOutToolbar(color);
	}

	/**
	 * Overrides the pending Activity transition by performing the "Enter" animation.
	 */
	protected void overridePendingTransitionEnter() {
		getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	/**
	 * Overrides the pending Activity transition by performing the "Exit" animation.
	 */
	protected void overridePendingTransitionExit() {
		getActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	public void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	public void popFragment() {
		getBottomNavigator().popFragment();
	}

	public WGlobalState getGlobalState() {
		return WoolworthsApplication.getInstance().getWGlobalState();
	}

	public void setStatusBarColor(int color) {
		getBottomNavigator().statusBarColor(color);
	}
}
