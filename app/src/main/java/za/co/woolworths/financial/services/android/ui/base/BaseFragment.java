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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.ContactUsCustomerServiceFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
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
		try {
			tv.setText(text);
		} catch (IllegalStateException ex) {
			Log.d("setTextExc", ex.getMessage());
		}
	}

	public void setText(String text, WTextView tv) {
		if (isEmpty(text)) {
			hideView(tv);
		} else {
			tv.setText(text);
			showView(tv);
		}
	}

	public void setText(WButton tv, String text) {
		try {
			tv.setText(text);
		} catch (IllegalStateException ex) {
			Log.d("setTextExc", ex.getMessage());
		}
	}

	public void setText(String text, WButton tv) {
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


	public void popFragmentNoAnim() {
		getBottomNavigator().popFragmentNoAnim();
	}

	public WGlobalState getGlobalState() {
		return WoolworthsApplication.getInstance().getWGlobalState();
	}

	public void setStatusBarColor(int color) {
		getBottomNavigator().statusBarColor(color);
	}

	public void addBadge(int position, int count) {
		getBottomNavigator().addBadge(position, count);
	}

	public int getCurrentStackIndex() {
		return getBottomNavigator().getCurrentStackIndex();
	}


	public void showToolbar(int id) {
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		setTitle(getString(id));
		showToolbar();
	}

	public void showToolbar(String title) {
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		setTitle(title);
		showToolbar();
	}

	public void showSoftKeyboard(WEditTextView editTextView) {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	public void showSoftKeyboard(WLoanEditTextView editTextView) {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	public void closeSoftKeyboard() {
		Activity act = getActivity();
		if (act != null && act.getCurrentFocus() != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
			inputMethodManager.showSoftInputFromInputMethod(act.getCurrentFocus().getWindowToken(), 0);
		}
	}

	public void showKeyboard(View view) {
		Activity act = getActivity();
		if (act != null) {
			new KeyboardUtil(act, view, getBottomNavigator().getBottomNavigationById().getHeight());
		}
	}

	public void sendBus(Object obj) {
		Utils.sendBus(obj);
	}

	public void observableOn(Consumer consumer){
		getViewModel().consumeObservable(consumer);
	}

}
