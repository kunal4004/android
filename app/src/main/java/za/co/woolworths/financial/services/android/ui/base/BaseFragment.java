package za.co.woolworths.financial.services.android.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.awfs.coordination.R;

import java.util.ArrayList;

import io.reactivex.functions.Consumer;
import retrofit2.Call;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.NetworkManager;
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
		setRetainInstance(true);
		mViewModel = getViewModel();
		setHasOptionsMenu(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {            // Inflate and populate
		try {
			mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
			mRootView = mViewDataBinding.getRoot();
			Activity activity = getActivity();
			if (activity != null) {
				bottomNavigator = (BottomNavigator) activity;
			}
		} catch (Exception ex) {
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
		Activity activity = getActivity();
		return (activity != null) && NetworkManager.getInstance().isConnectedToNetwork(activity);
	}

	public void hideToolbar() {
		if (getActivity() instanceof BottomNavigationActivity) {
			mActivity.hideToolbar();
		}
	}

	public void showToolbar() {
		if (getActivity() instanceof BottomNavigationActivity) {
			statusBarColor();
			mActivity.showToolbar();
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
		if (bottomNavigator != null && (getActivity() instanceof BottomNavigationActivity))
			bottomNavigator.showBackNavigationIcon(visibility);
	}

	public void setTitle(String title) {
		if (bottomNavigator != null && (getActivity() instanceof BottomNavigationActivity))
			bottomNavigator.setTitle(title);
	}

	public void setTitle(String title, int color) {
		if (bottomNavigator != null && (getActivity() instanceof BottomNavigationActivity))
			bottomNavigator.setTitle(title, color);
	}

	public void setToolbarBackgroundDrawable(int drawable) {
		if (mActivity != null && (getActivity() instanceof BottomNavigationActivity)) {
			mActivity.setToolbarBackgroundDrawable(drawable);
		}
	}

	public void setToolbarBackgroundColor(int color) {
		if (getActivity() instanceof BottomNavigationActivity) {
			mActivity.setToolbarBackgroundColor(color);
		}
	}

	public BottomNavigator getBottomNavigator() {
		return bottomNavigator;
	}

	public void showView(View view) {
		// This method may be called when not needed.
		// Handle modifying UI only when needed.
		if (view != null && view.getVisibility() != View.VISIBLE)
			view.setVisibility(View.VISIBLE);
	}

	public void hideView(View view) {
		// This method may be called when not needed.
		// Handle modifying UI only when needed.
		if (view != null && view.getVisibility() != View.GONE)
			view.setVisibility(View.GONE);
	}

	public void slideBottomPanel() {
		if (bottomNavigator != null)
			bottomNavigator.slideUpBottomView();
	}

	public boolean isEmpty(String value) {
		return TextUtils.isEmpty(value);
	}

	public void pushFragment(Fragment fragment) {
		getBottomNavigator().pushFragment(fragment);
	}

	public void pushFragmentSlideUp(Fragment fragment) {
		getBottomNavigator().pushFragmentSlideUp(fragment);
	}

	public void pushFragmentNoAnim(Fragment fragment) {
		getBottomNavigator().pushFragmentNoAnim(fragment);
	}

	public void setText(WTextView tv, String text) {
		try {
			tv.setText(text);
		} catch (IllegalStateException ex) {
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

	public void cancelRequest(Call call) {
		if (call != null && !call.isCanceled()) {
				call.cancel();
			}
	}

	public void popFragment() {
		getBottomNavigator().popFragment();
	}


	public void popFragmentNoAnim() {
		getBottomNavigator().popFragmentNoAnim();
	}

	public void popFragmentSlideDown() {
		getBottomNavigator().popFragmentSlideDown();
	}

	public WGlobalState getGlobalState() {
		return WoolworthsApplication.getInstance().getWGlobalState();
	}

	public void setStatusBarColor(int color) {
		getBottomNavigator().statusBarColor(color);
	}

	public void setStatusBarColor(int color, boolean enableDecor) {
		getBottomNavigator().statusBarColor(color, enableDecor);
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
		statusBarColor();
		mActivity.showToolbar();
	}

	private void statusBarColor() {
		Utils.updateStatusBarBackground(getActivity());
	}

	private void statusBarColor(int color) {
		Utils.updateStatusBarBackground(getActivity(), color);
	}

	public void showToolbar(int id, int color) {
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(color);
		setTitle(getString(id), color);
		mActivity.showToolbar();
	}

	public void showToolbar(String title) {
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);
		setTitle(title);
		statusBarColor();
		if (mActivity != null)
			mActivity.showToolbar();
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

	public void observableOn(Consumer consumer) {
		getViewModel().consumeObservable(consumer);
	}


	public void checkLocationPermission(BottomNavigator bottomNavigator, ArrayList<String> permissionType, int request_code) {
		bottomNavigator.getRuntimePermission().checkPermission(permissionType, request_code);
	}

	public void slideDownOnToolbarNavigationOnClickListener() {
		Activity activity = getActivity();
		if (activity != null) {
			BottomNavigationActivity bottomNavigationActivity = ((BottomNavigationActivity) activity);
			bottomNavigationActivity.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					popFragmentSlideDown();
				}
			});
		}
	}

	public void slideStartOnToolbarNavigationClickListener() {
		Activity activity = getActivity();
		if (activity != null) {
			BottomNavigationActivity bottomNavigationActivity = ((BottomNavigationActivity) activity);
			bottomNavigationActivity.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					popFragment();
				}
			});
		}
	}

	public BottomNavigationActivity getBottomNavigationActivity() {
		Activity activity = getActivity();
		if (activity instanceof BottomNavigationActivity) {
			return (BottomNavigationActivity) activity;
		}
		return null;
	}
	public void setToolBarTitleUniqueID(String contentDescID){
		mActivity.getToolbar().findViewById(R.id.tvToolbar).setContentDescription(contentDescID);
	}
}
