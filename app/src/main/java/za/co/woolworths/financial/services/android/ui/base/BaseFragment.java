package za.co.woolworths.financial.services.android.ui.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.base.back_press_impl.BackFragment;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.Utils;

public abstract class BaseFragment<T extends ViewDataBinding, V extends BaseViewModel> extends BackFragment {

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
							 Bundle savedInstanceState) {
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

	public BottomNavigator getBottomNavigator() {
		return bottomNavigator;
	}

	public void showView(View view) {
		view.setVisibility(View.VISIBLE);
	}

	public void hideView(View view) {
		view.setVisibility(View.GONE);
	}

	public void slideBottomPanel() {
		bottomNavigator.slideUpBottomView();
	}
}
